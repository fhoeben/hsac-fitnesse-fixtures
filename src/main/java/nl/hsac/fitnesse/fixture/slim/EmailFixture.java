package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.slim.exceptions.CouldNotFindMessageException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class EmailFixture extends SlimFixture {
    private static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static Date HOURS_BACK = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    private Store store;

    /**
     * | set mail provider with host | <i>host</i> | user | <i>username</i> | password | <i>password</i> |
     */
    public void setMailProviderWithHostPortUserPassword(String host, String username, String password) {
        Properties props = new Properties();
        try {
            Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("imaps");
            store.connect(host, username, password);
        } catch (MessagingException e) {
            throw new StopTestException("Cannot connect to mailserver", e);
        }
    }

    /**
     * | ensure | mail received by | <i>receiver</i> | with subject | <i>subject</i> |
     *
     * @param subject
     * @return
     */
    public String mailReceivedByWithSubject(String receiver, String subject) {
        return getMailText(new SearchParameters(subject, receiver, HOURS_BACK));
    }

    /**
     * | $result= | extract text with | <i>regex</i> | from mail with | <i>subject</i> | from | <i>receiver</i> |
     */
    public String extractTextWithFromMailWithFrom(String regex, String subject, String receiver) {
        String text = getMailText(new SearchParameters(subject, receiver, HOURS_BACK));
        return getText(text, regex);
    }

    private String getText(String text, String regexpattern) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Text found: " + text);
            LOGGER.debug("Regex: " + regexpattern);
        }
        Pattern pattern = Pattern.compile(regexpattern);
        Matcher matcher = pattern.matcher(text);
        boolean isMatch = matcher.find();
        if (isMatch) {
            return matcher.group(1);
        }
        throw new SlimFixtureException(false, format("No match found in text %s with regex %s", text, regexpattern));
    }

    private String getMailText(SearchParameters params) {
        try {
            Folder inbox = getInboxFolder();
            Message msg = getRecentMessagesMatching(inbox, params);
            return getBody(msg);
        } catch (Exception ex) {
            throw new SlimFixtureException(false, "No message found with search params: " + params.toString(), ex);
        }
    }

    private Folder getInboxFolder() throws MessagingException {
        Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_ONLY);
        return inbox;
    }

    private Message getRecentMessagesMatching(Folder inbox, SearchParameters params) {
        List<Message> mails = getMessagesMatchingAndWaitUntil(inbox, params);
        Collections.reverse(mails);
        return getMostRecentMessage(mails);
    }

    private Message getMostRecentMessage(List<Message> mails) {
        try {
            for (Message mail : mails) {
                if (mail.getSentDate().after(HOURS_BACK)) {
                    return mail;
                }
            }
        } catch (MessagingException ex) {
            throw new RuntimeException("Exception", ex);
        }
        throw new RuntimeException();
    }

    private List<Message> getMessagesMatchingAndWaitUntil(Folder inbox, SearchParameters params) {
        FetchEmailsStrategy fetchEmails = new FetchEmailsStrategy(inbox, params);
        boolean pretty = repeatUntil(fetchEmails);
        if (!pretty)
            throw new CouldNotFindMessageException(inbox, params);
        return fetchEmails.getResult();
    }

    private class FetchEmailsStrategy implements RepeatCompletion {
        private Folder inbox;
        private SearchParameters params;
        private List<Message> mails;

        FetchEmailsStrategy(Folder inbox, SearchParameters params) {
            this.inbox = inbox;
            this.params = params;
        }

        public List<Message> getResult() {
            return mails;
        }

        @Override
        public boolean isFinished() {
            return mails != null && mails.size() > 0;
        }

        @Override
        public void repeat() {
            try {
                mails = getMessagesMatching(inbox, params);
            } catch (RuntimeException ex) {
            }
        }
    }

    private List<Message> getMessagesMatching(Folder inbox, SearchParameters params) {
        try {
            SearchTerm searchCondition = params.getSearchTerm();
            return Arrays.asList(inbox.search(searchCondition, inbox.getMessages()));
        } catch (RuntimeException | MessagingException ex) {
            throw new RuntimeException("Exception", ex);
        }
    }


    private String getBody(Message msg) {
        try {
            return tryBody(msg);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (MessagingException ex) {
            throw new RuntimeException("Exception", ex);
        }
    }

    private String tryBody(Message msg) throws IOException, MessagingException {
        String message = "";
        if (msg != null && msg.getContent() instanceof MimeMultipart) {
            Multipart multipart = (Multipart) msg.getContent();
            message = IOUtils.toString(multipart.getBodyPart(0).getInputStream());
        }
        if (message != null) {
            message = msg.getContent().toString();
        }
        return message;
    }

    public static class SearchParameters {
        private String subject;
        private String receiver;
        private Date startFromDate;

        public SearchParameters(String subject, String receiver, Date startFromDate) {
            this.subject = subject;
            this.receiver = receiver;
            this.startFromDate = startFromDate;
        }

        private SearchTerm getSearchTerm() {
            return new SearchTerm() {
                private final static long serialVersionUID = -2333952189183496834L;

                @Override
                public boolean match(Message message) {
                    try {
                        return message.getSubject().contains(subject)
                                && message.getReceivedDate().after(startFromDate)
                                && message.getRecipients(Message.RecipientType.TO)[0].toString().contains(receiver);
                    } catch (MessagingException ex) {
                        throw new IllegalStateException("No match, message not found.." + ex.getMessage());
                    }
                }
            };
        }

        @Override
        public String toString() {
            return format("subject='%s', receive='%s', startFromDate='%s'", subject, receiver, startFromDate);
        }
    }
}

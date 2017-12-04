package nl.hsac.fitnesse.fixture.slim;

import nl.hsac.fitnesse.fixture.slim.exceptions.CouldNotFindMessageException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

public class EmailFixture extends SlimFixture {
    private static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static Date HOURS_BACK = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    private Store store;

    /**
     * | set imap mail provider with host | <i>host</i> | user | <i>username</i> | password | <i>password</i> |
     */
    public void setImapMailProviderWithHostPortUserPassword(String host, String username, String password) {
        try {
            store = getStore();
            store.connect(host, username, password);
        } catch (MessagingException e) {
            throw new StopTestException("Cannot connect to mailserver", e);
        }
    }

    protected Store getStore() throws NoSuchProviderException {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props);
        return session.getStore("imaps");
    }

    /**
     * | ensure | mail received by | <i>receiver</i> | with subject | <i>subject</i> |
     */
    public String mailReceivedByWithSubject(String receiver, String subject) {
        return getMailText(new SearchParameters(subject, receiver, HOURS_BACK));
    }

    /**
     * | $result= | extract text with | <i>regex</i> | from mail received by | <i>receiver</i> | with subject | <i>subject</i> |
     */
    public String extractTextWithFromMailReceivedByWithSubject(String regex, String receiver, String subject) {
        String text = mailReceivedByWithSubject(subject, receiver);
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
            throw new SlimFixtureException(false, "No message found with search params: " + params, ex);
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
            throw new RuntimeException("Exception looking for mail sent after: " + HOURS_BACK, ex);
        }
        throw new SlimFixtureException(false, "No mail found sent after: " + HOURS_BACK);
    }

    private List<Message> getMessagesMatchingAndWaitUntil(Folder inbox, SearchParameters params) {
        List<Message> mails = new ArrayList<>();
        if (!repeatUntilNot(new FunctionalCompletion(
                mails::isEmpty,
                () -> mails.addAll(getMessagesMatching(inbox, params))))) {
            throw new CouldNotFindMessageException(inbox, params);
        }
        return mails;
    }

    private List<Message> getMessagesMatching(Folder inbox, SearchParameters params) {
        try {
            SearchTerm searchCondition = params.getSearchTerm();
            return Arrays.asList(inbox.search(searchCondition, inbox.getMessages()));
        } catch (MessagingException ex) {
            throw new RuntimeException("Exception retrieving mail with parameters: " + params, ex);
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
        if (msg != null) {
            Object msgContent = msg.getContent();
            if (msgContent instanceof MimeMultipart) {
                Multipart multipart = (Multipart) msgContent;
                message = IOUtils.toString(multipart.getBodyPart(0).getInputStream());
            } else {
                message = msgContent.toString();
            }
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
                        throw new IllegalStateException("No match, message not found.." + ex.getMessage(), ex);
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

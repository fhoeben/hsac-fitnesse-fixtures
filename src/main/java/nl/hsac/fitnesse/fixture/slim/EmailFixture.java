package nl.hsac.fitnesse.fixture.slim;

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

public class EmailFixture extends SlimFixture {
    private final static Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private static final Date HOURS_BACK = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    private Store store;

    public EmailFixture() {

    }

    /**
     * | set mail provider with host | <i>host</i> | port | <i>port</i> | user | <i>username</i> | password | <i>password</i> |
     */
    public void setMailProviderWithHostPortUserPassword(final String host, final String port, final String username, final String password) {
        final Properties props = new Properties();
        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.socketFactory.port", port);
        props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.auth", "true");
        props.setProperty("mail.smtp.port", port);

        try {
            final Session session = Session.getDefaultInstance(props, null);
            store = session.getStore("imaps");
            store.connect("smtp.gmail.com", username, password);
        } catch (final MessagingException e) {
            throw new StopTestException("Cannot connect to mailserver");
        }
    }

    /**
     * | ensure | mail received with subject | <i>subject</i> | from | <i>receiver</i> |
     *
     * @param subject
     * @return
     */
    public String mailReceivedWithSubjectFrom(final String subject, final String receiver) {
        return getMailText(new SearchParameters(subject, receiver, HOURS_BACK));
    }

    /**
     * | $result= | extract text with | <i>regex</i> | from mail with | <i>subject</i> | from | <i>receiver</i> |
     */
    public String extractTextWithFromMailWithFrom(final String regex, final String subject, final String receiver) {
        final String text = getMailText(new SearchParameters(subject, receiver, HOURS_BACK));
        return getText(text, regex);
    }

    private String getText(final String text, final String regexpattern) {
        LOGGER.debug("Text found: " + text);
        LOGGER.debug("Regex: " + regexpattern);
        final Pattern pattern = Pattern.compile(regexpattern);
        final Matcher matcher = pattern.matcher(text);
        final boolean isMatch = matcher.find();
        if (isMatch) {
            return matcher.group(1);
        }
        throw new SlimFixtureException(false, "No match found");
    }

    private String getMailText(SearchParameters params) {
        try {
            final Folder inbox = getInboxFolder();
            Message msg = getRecentMessagesMatching(inbox, params);
            return getBody(msg);
        } catch (final Exception e) {
            throw new SlimFixtureException(false, "No message found with search params: " + params.toString());
        }
    }

    private Folder getInboxFolder() throws MessagingException {
        final Folder inbox = store.getFolder("inbox");
        inbox.open(Folder.READ_WRITE);
        return inbox;
    }

    private Message getRecentMessagesMatching(Folder inbox, SearchParameters params) throws IOException, MessagingException {
        List<Message> mails = getMessagesMatching(inbox, params);
        Collections.reverse(mails);
        return getMostRecentMessage(mails);
    }

    private Message getMostRecentMessage(List<Message> mails) throws MessagingException {
        for (final Message mail : mails) {
            if (mail.getSentDate().after(HOURS_BACK)) {
                return mail;
            }
        }
        return null;
    }

    private List<Message> getMessagesMatching(Folder inbox, SearchParameters params) throws IOException, MessagingException {
        List<Message> mails = new ArrayList<>();
        int tries = 0;
        while ((mails.size() == 0) && (tries <= 45)) {
            mails = getMessagesMatchingSearchCondiiton(inbox, params);
            tries++;
            try {
                Thread.sleep(1000);
            } catch (final InterruptedException e) {
                LOGGER.debug("Mail cannot be found!: " + e.getMessage());
            }
        }
        return mails;
    }

    private List<Message> getMessagesMatchingSearchCondiiton(Folder inbox, SearchParameters params) throws IOException, MessagingException {
        final SearchTerm searchCondition = params.getSearchTerm();
        return Arrays.asList(inbox.search(searchCondition, inbox.getMessages()));
    }


    private String getBody(Message msg) throws IOException, MessagingException {
        String message = "";
        if (msg != null && msg.getContent() instanceof MimeMultipart) {
            final Multipart multipart = (Multipart) msg.getContent();
            message = IOUtils.toString(multipart.getBodyPart(0).getInputStream());
        }
        if (message != null) {
            message = msg.getContent().toString();
        }
        return message;

    }

    private class SearchParameters {
        private final String subject;
        private final String receiver;
        private final Date startFromDate;

        public SearchParameters(String subject, String receiver, Date startFromDate) {
            this.subject = subject;
            this.receiver = receiver;
            this.startFromDate = startFromDate;
        }

        private SearchTerm getSearchTerm() {
            return new SearchTerm() {
                private static final long serialVersionUID = -2333952189183496834L;

                @Override
                public boolean match(final Message message) {
                    try {
                        return message.getSubject().contains(subject)
                                && message.getReceivedDate().after(startFromDate)
                                && message.getRecipients(Message.RecipientType.TO)[0].toString().contains(receiver);
                    } catch (final MessagingException ex) {
                        throw new IllegalStateException("No match, message not found.." + ex.getMessage());
                    }
                }
            };
        }

        @Override
        public String toString() {
            return "SearchParameters{" +
                    "subject='" + subject + '\'' +
                    ", receiver='" + receiver + '\'' +
                    ", startFromDate=" + startFromDate +
                    '}';
        }
    }
}

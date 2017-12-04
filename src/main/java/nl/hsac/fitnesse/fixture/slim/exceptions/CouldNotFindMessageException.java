package nl.hsac.fitnesse.fixture.slim.exceptions;

import nl.hsac.fitnesse.fixture.slim.EmailFixture;
import nl.hsac.fitnesse.fixture.slim.SlimFixtureException;

import javax.mail.Folder;

import static java.lang.String.format;

public class CouldNotFindMessageException extends SlimFixtureException {
    public CouldNotFindMessageException(Folder inbox, EmailFixture.SearchParameters params) {
        super(format("Could not find mail in '%s' folder with search parameters: %s", inbox.getName(), params.toString()));
    }
}

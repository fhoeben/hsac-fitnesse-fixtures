package nl.hsac.fitnesse.fixture;

import fit.ColumnFixture;

/**
  * Waits before continuing to the next fixture.
 */ 
public class WaitFixture extends ColumnFixture {
    public int waitTimeSeconds;
   
    @Override
    public void execute() throws Exception { 
        waitSeconds();
        super.execute();     
    }
    
    public void waitSeconds() {
        try {
            Thread.sleep(waitTimeSeconds * 1000l);
        } catch (InterruptedException e) {
            throw new RuntimeException("Waiting interrupted", e);
        }
    }
    public int waitTimeSeconds(){
        return waitTimeSeconds;
    }
}

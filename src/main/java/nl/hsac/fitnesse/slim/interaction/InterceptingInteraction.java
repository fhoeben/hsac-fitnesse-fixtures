package nl.hsac.fitnesse.slim.interaction;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Own interaction to allow all calls from Slim to a fixture to be intercepted.
 * This allows for 'aspect oriented' solutions to common features instead of having to
 * put that in each fixture method.
 * This is a temporary solution. Once a FitNesse release with https://github.com/unclebob/fitnesse/pull/724
 * is used, we can use the InteractionAwareFixture present there.
 */
public class InterceptingInteraction extends DefaultInteraction {
    @Override
    public Object methodInvoke(Method method, Object instance, Object... convertedArgs)
            throws InvocationTargetException, IllegalAccessException {
        Object result;
        if (instance instanceof InteractionAwareFixture
                && !"aroundSlimInvoke".equals(method.getName())) {
            InteractionAwareFixture fixture = (InteractionAwareFixture) instance;
            FixtureInteraction interaction = getInteraction();
            result = fixture.aroundSlimInvoke(interaction, method, convertedArgs);
        } else {
            result = super.methodInvoke(method, instance, convertedArgs);
        }
        return result;
    }

    protected FixtureInteraction getInteraction() {
        return new DefaultInteraction();
    }
}

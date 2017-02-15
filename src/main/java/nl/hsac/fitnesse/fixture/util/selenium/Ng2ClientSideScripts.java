package nl.hsac.fitnesse.fixture.util.selenium;

/**
 * Javascript scripts to perform against Angular 2+ sites when using Selenium
 * (copied from @link https://github.com/bbaia/protractor-net/blob/master/src/Protractor/ClientSideScripts.cs
 * and updated using @link https://github.com/angular/protractor/blob/master/lib/clientsidescripts.js).
 */
public class Ng2ClientSideScripts {
    /**
     * Waits (asynchronously) until Angular has finished rendering and has
     * no outstanding http calls before continuing.
     *
     * arguments[0] {string} The selector housing Angular application
     * arguments[1] {function} callback
     */
    public final static String WaitForAngular =
        "var rootSelector = arguments[0];\n" +
        "  try {\n" +
        "    if (rootSelector && window.getAngularTestability) {\n" +
        "      var el = document.querySelector(rootSelector);\n" +
        "      window.getAngularTestability(el).whenStable(callback);\n" +
        "    } else if (window.getAllAngularTestabilities) {\n" +
        "      var testabilities = window.getAllAngularTestabilities();\n" +
        "      var count = testabilities.length;\n" +
        "      var decrement = function() {\n" +
        "        count--;\n" +
        "        if (count === 0) {\n" +
        "          callback();\n" +
        "        }\n" +
        "      };\n" +
        "      testabilities.forEach(function(testability) {\n" +
        "        testability.whenStable(decrement);\n" +
        "      });\n" +
        "    } else if (!window.angular) {\n" +
        "      throw new Error('window.angular is undefined.  This could be either ' +\n" +
        "          'because this is a non-angular page or because your test involves ' +\n" +
        "          'client-side navigation, which can interfere with Protractor\\'s ' +\n" +
        "          'bootstrapping.  See http://git.io/v4gXM for details');\n" +
        "    } else if (window.angular.version >= 2) {\n" +
        "      throw new Error('You appear to be using angular, but window.' +\n" +
        "          'getAngularTestability was never set.  This may be due to bad ' +\n" +
        "          'obfuscation.');\n" +
        "    } else {\n" +
        "      throw new Error('Cannot get testability API for unknown angular ' +\n" +
        "          'version \"' + window.angular.version + '\"');\n" +
        "    }\n" +
        "  } catch (err) {\n" +
        "    callback(err.message);\n" +
        "  }";  
}
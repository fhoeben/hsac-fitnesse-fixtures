package nl.hsac.fitnesse.fixture.util;

/**
 * Javascript scripts to perform against AngularJs sites when using Selenium
 * (copied from @link https://github.com/bbaia/protractor-net/blob/master/src/Protractor/ClientSideScripts.cs).
 */
public class NgClientSideScripts {
    /**
     * Waits (asynchronously) until Angular has finished rendering and has
     * no outstanding $http calls before continuing.
     *
     * arguments[0] {string} The selector housing an ng-app
     * arguments[1] {function} callback
     */
    public final static String WaitForAngular =
            "var el = document.querySelector(arguments[0]);\n" +
            "angular.element(el).injector().get('$browser').notifyWhenNoOutstandingRequests(callback);";

    /**
     * Tests (asynchronously) whether the angular global variable is present on a page.
     * Retries in case the page is just loading slowly.
     *
     * arguments[0] {string} none.
     */
    public final static String TestForAngular =
            "var attempts = arguments[0];\n" +
            "var check = function(n) {\n" +
            "    if (window.angular && window.angular.resumeBootstrap) {\n" +
            "        callback(true);\n" +
            "    } else if (n < 1) {\n" +
            "        callback(false);\n" +
            "    } else {\n" +
            "        window.setTimeout(function() {check(n - 1)}, 1000);\n" +
            "    }\n" +
            "};\n" +
            "check(attempts);";

    /**
     * Continue to bootstrap Angular.
     *
     * arguments[0] {array} The module names to load.
     */
    public final static String ResumeAngularBootstrap =
            "angular.resumeBootstrap(arguments[0].length ? arguments[0].split(',') : []);";

    /**
     * Return the current url using $location.absUrl().
     *
     * arguments[0] {string} The selector housing an ng-app
     */
    public final static String GetLocationAbsUrl =
            "var el = document.querySelector(arguments[0]);\n" +
            "return angular.element(el).injector().get('$location').absUrl();";

    /**
     * Evaluate an Angular expression in the context of a given element.
     *
     * arguments[0] {Element} The element in whose scope to evaluate.
     * arguments[1] {string} The expression to evaluate.
     *
     * @return {?Object} The result of the evaluation.
     */
    public final static String Evaluate =
            "var element = arguments[0];\n" +
            "var expression = arguments[1];\n" +
            "return angular.element(element).scope().$eval(expression);";

    /**
     * Find a list of elements in the page by their angular binding.
     *
     * arguments[0] {Element} The scope of the search.
     * arguments[1] {string} The binding, e.g. {{cat.name}}.
     *
     * @return {Array.WebElement} The elements containing the binding.
     */
    public final static String FindBindings =
        "var using = arguments[0] || document;\n" +
        "var binding = arguments[1];\n" +
        "var bindings = using.getElementsByClassName('ng-binding');\n" +
        "var matches = [];\n" +
        "for (var i = 0; i < bindings.length; ++i) {\n" +
        "    var bindingName = angular.element(bindings[i]).data().$binding[0].exp ||\n" +
        "            angular.element(bindings[i]).data().$binding;\n" +
        "    if (bindingName.indexOf(binding) != -1) {\n" +
        "        matches.push(bindings[i]);\n" +
        "    }\n" +
        "}\n" +
        "return matches;";

    /**
     * Find input elements by model name.
     *
     * arguments[0] {Element} The scope of the search.
     * arguments[1] {string} The model name.
     *
     * @return {Array.WebElement} The matching input elements.
     */
    public final static String FindInputs =
        "var using = arguments[0] || document;\n" +
        "var model = arguments[1];\n" +
        "var prefixes = ['ng-', 'ng_', 'data-ng-', 'x-ng-', 'ng\\\\:'];\n" +
        "for (var p = 0; p < prefixes.length; ++p) {\n" +
        "    var selector = 'input[' + prefixes[p] + 'model=\"\"' + model + '\"\"]';\n" +
        "    var inputs = using.querySelectorAll(selector);\n" +
        "    if (inputs.length) {\n" +
        "        return inputs;\n" +
        "    }\n" +
        "}";

    /**
     * Find multiple select elements by model name.
     *
     * arguments[0] {Element} The scope of the search.
     * arguments[1] {string} The model name.
     *
     * @return {Array.WebElement} The matching select elements.
     */
    public final static String FindSelects =
        "var using = arguments[0] || document;\n" +
        "var model = arguments[1];\n" +
        "var prefixes = ['ng-', 'ng_', 'data-ng-', 'x-ng-', 'ng\\\\:'];\n" +
        "for (var p = 0; p < prefixes.length; ++p) {\n" +
        "    var selector = 'select[' + prefixes[p] + 'model=\"\"' + model + '\"\"]';\n" +
        "    var inputs = using.querySelectorAll(selector);\n" +
        "    if (inputs.length) {\n" +
        "        return inputs;\n" +
        "    }\n" +
        "}";

    /**
     * Find selected option elements by model name.
     *
     * arguments[0] {Element} The scope of the search.
     * arguments[1] {string} The model name.
     *
     * @return {Array.WebElement} The matching select elements.
     */
    public final static String FindSelectedOptions =
        "var using = arguments[0] || document;\n" +
        "var model = arguments[1];\n" +
        "var prefixes = ['ng-', 'ng_', 'data-ng-', 'x-ng-', 'ng\\\\:'];\n" +
        "for (var p = 0; p < prefixes.length; ++p) {\n" +
        "    var selector = 'select[' + prefixes[p] + 'model=\"\"' + model + '\"\"] option:checked';\n" +
        "    var inputs = using.querySelectorAll(selector);\n" +
        "    if (inputs.length) {\n" +
        "        return inputs;\n" +
        "    }\n" +
        "}";

    /**
     * Find textarea elements by model name.
     *
     * arguments[0] {Element} The scope of the search.
     * arguments[1] {String} The model name.
     *
     * @return {Array.WebElement} The matching textarea elements.
     */
    public final static String FindTextArea =
        "var using = arguments[0] || document;\n" +
        "var model = arguments[1];\n" +
        "var prefixes = ['ng-', 'ng_', 'data-ng-', 'x-ng-', 'ng\\\\:'];\n" +
        "for (var p = 0; p < prefixes.length; ++p) {\n" +
        "    var selector = 'textarea[' + prefixes[p] + 'model=\"\"' + model + '\"\"]';\n" +
        "    var textareas = using.querySelectorAll(selector);\n" +
        "    if (textareas.length) {\n" +
        "        return textareas;\n" +
        "    }\n" +
        "}";

    /**
     * Find all rows of an ng-repeat.
     *
     * arguments[0] {Element} The scope of the search.
     * arguments[1] {string} The text of the repeater, e.g. 'cat in cats'.
     *
     * @return {Array.WebElement} All rows of the repeater.
     */
    public final static String FindAllRepeaterRows =
        "var using = arguments[0] || document;\n" +
        "var repeater = arguments[1];\n" +
        "var rows = [];\n" +
        "var prefixes = ['ng-', 'ng_', 'data-ng-', 'x-ng-', 'ng\\\\:'];\n" +
        "for (var p = 0; p < prefixes.length; ++p) {\n" +
        "    var attr = prefixes[p] + 'repeat';\n" +
        "    var repeatElems = using.querySelectorAll('[' + attr + ']');\n" +
        "    attr = attr.replace(/\\\\/g, '');\n" +
        "    for (var i = 0; i < repeatElems.length; ++i) {\n" +
        "        if (repeatElems[i].getAttribute(attr).indexOf(repeater) != -1) {\n" +
        "            rows.push(repeatElems[i]);\n" +
        "        }\n" +
        "    }\n" +
        "}\n" +
        "return rows;";
}

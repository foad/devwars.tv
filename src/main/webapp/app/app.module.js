var app = angular.module('app', [
// modules
    'app.AuthService',
    'app.header',
    'app.home',
    'app.signup',
    'app.gameControlPanel',
    'app.GameService',
    'app.BlogService',
    'app.InfoService',
    'app.UserService',
    'app.WarriorService',
    'app.ContactService',
    'app.games',
    'app.gameSignupConfirm',
    'app.editPlayerDialog',
    'app.addPlayerDialog',
    'app.addSelectedPlayerDialog',
    'app.addBlogPostDialog',
    'app.InputDialog',
    'app.devwarsToast',
    'app.toastService',
    'app.BadgeService',
    'app.blog',
    'app.user',
    'pickadate',
    'app.contact',
    'app.about',
    'app.dashboard',
    'app.badges',
    'app.warriorReg',
    'app.DialogService',
    'app.settings',
    'app.help',
    'app.liveGame',
    'app.verify',
    'app.enterDirective',
    'app.subscribeDirective',
    'app.shop',
    'app.OAuthDirective',
    'app.sidebar',
    'app.editAvatarImage',
    'app.fileread',
    'app.rank',
    'app.profile',
    'app.dashnav',
    'app.confirmDialog',
    'app.leaderboards',
    'app.modCP',

    //Poll
    'LivePoll-Client',
    'LivePoll-Display',
    'Progress',
    'Tooltip',

    //dependencies
    'ngCookies',
    'ngMaterial',
    'vcRecaptcha',
    'textAngular',
    'n3-pie-chart',
    'ngImgCrop'
]);

app.config(['$urlRouterProvider', '$httpProvider', '$locationProvider', function ($urlRouterProvider, $httpProvider, $locationProvider) {
    // all page specific routes are in their js file
    $urlRouterProvider.otherwise('/');

    $httpProvider.defaults.transformResponse = function (response) {
        try {
            return JSON.parse(response);
        } catch (exception) {
            return response;
        }
    };

    $httpProvider.defaults.transformRequest = function (obj) {
        var str = [];
        for(var p in obj)
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
        return str.join("&");
    };

    $httpProvider.defaults.headers.post['Content-Type'] = "application/x-www-form-urlencoded";

    $locationProvider.html5Mode(true);
}]);

app.filter("reverse", function () {
    return function (data) {
        return data.reverse();
    }
});

app.filter("players", function () {
    return _.memoize(function (data) {
        var langs = ["html", "css", "js"];

        data.sort(function (a) {
            return langs.indexOf(a.language.toLowerCase());
        });

        return data;
    })
});
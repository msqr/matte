// see http://stackoverflow.com/questions/11291651/why-dont-audio-and-video-events-bubble
$.createEventCapturing = (function () {
    var special = $.event.special;
    return function (names) {
        if (!document.addEventListener) {
            return;
        }
        if (typeof names == 'string') {
            names = [names];
        }
        $.each(names, function (i, name) {
            var handler = function (e) {
                e = $.event.fix(e);

                return $.event.dispatch.call(this, e);
            };
            special[name] = special[name] || {};
            if (special[name].setup || special[name].teardown) {
                return;
            }
            $.extend(special[name], {
                setup: function () {
                    this.addEventListener(name, handler, true);
                },
                teardown: function () {
                    this.removeEventListener(name, handler, true);
                }
            });
        });
    };
})();

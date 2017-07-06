(function () {
  'use strict';

  Counter.$inject = ['$compile', '$timeout'];
  function Counter($compile, $timeout) {
    return {
      restrict: 'A',
      require: ['^form', 'ngModel'],
      link: function (scope, elem, attrs) {
        scope._max = parseInt(attrs.gumgaMaxLengthText);

        if (!isNaN(parseInt(attrs.gumgaCounter))) {
          scope._max = parseInt(attrs.gumgaCounter);
        }

        var template = '<p class="{{_max <= teste.length ? \'text-danger\' : \'text-muted\'}}">{{_max <= ' + attrs.ngModel + '.length ? "Você atingiu o limite de ' + scope._max + ' caracteres" : _max - ' + attrs.ngModel + '.length + " caracteres restantes" }}</p>';
        elem.after($compile(template)(scope));

        angular.element(elem).keyup(($event) => {
          if($event.target.value && $event.target.value.length > scope._max){
             $event.target.value = $event.target.value.substring(0, scope._max);
             $timeout(() => scope.ngModel = $event.target.value, 10);
          }
        })

      }
    };
  }
  angular.module('gumga.counter', [])
    .directive('gumgaCounter', Counter);
})();

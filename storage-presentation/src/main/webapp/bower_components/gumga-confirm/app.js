angular.module('app', ['ui.bootstrap','gumga.confirm'])
  .controller('Ctrl', function () {
    var ctrl = this;

    ctrl.remove = function(){
      alert('removeu');
    }

  })

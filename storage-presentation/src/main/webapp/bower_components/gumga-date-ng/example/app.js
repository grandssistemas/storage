(function(angular){

    angular.module('app', ['gumga.date'])
      .config(function(GumgaDateServiceProvider){
        GumgaDateServiceProvider.setDefaultConfiguration({
          fontColor: '#000'
        })
      })
      .controller('ctrl', function($scope) {

        $scope.nascimento = new Date("2017-03-25T10:30:00-03:00");

        $scope.teste = function(){
          $scope.nascimento = new Date("2017-03-25T10:30:00-03:00");
        }

        // LEFT_BOTTOM,LEFT_TOP,BOTTOM_LEFT,BOTTOM_RIGHT,RIGHT_BOTTOM,RIGHT_TOP,TOP_LEFT,TOP_RIGHT

        $scope.config = {
          fontColor: '#fff',
          format: 'dd/MM/yyyy HH:mm',
          position: 'BOTTOM_LEFT',
          closeOnChange : false,
          changeDateOnTab: true,
          showCalendar: true,
          timeZone: "America/Sao_Paulo",
          change: function(data){
            // console.log(data)
          }
        }

        $scope.config2 = {
          fontColor: '#fff',
          format: 'HH:mm',
          position: 'BOTTOM_LEFT',
          showCalendar: true,
          timeZone: "America/Sao_Paulo",
          change: function(data){
            // console.log(data)
          }
        }

      });

})(window.angular);

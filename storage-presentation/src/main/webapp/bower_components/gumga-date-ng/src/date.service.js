(() => {
  'use strict';

  const GumgaService = () => {

      const configuration = {
        background: '#1abc9c',
        primaryColor: '#1abc9c',
        fontColor: '#fff',
        format: 'dd/MM/yyyy',
        minYear: 1700,
        timeZone: "America/Sao_Paulo",
        maxYear: 2050,
        position: 'BOTTOM_LEFT',
        changeDateOnTab: false,
        showCalendar: true,
        closeOnChange: false,
        inputProperties: {
          class: 'form-control gmd'
        }
      }

      const getDefaultConfiguration = () => {
        return configuration;
      }

      const setDefaultConfiguration = config => {
        Object.keys(config).forEach(key => configuration[key] = config[key]);
      }

      return {
        getDefaultConfiguration : getDefaultConfiguration,
        setDefaultConfiguration : setDefaultConfiguration,
        $get : function(){
          return {
            getDefaultConfiguration : getDefaultConfiguration,
            setDefaultConfiguration : setDefaultConfiguration
          }
        }
      };

  }

  angular.module('gumga.date.service', [])
         .provider('GumgaDateService', GumgaService);
})();

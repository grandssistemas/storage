angular.module('app', ['gumga.genericfilter'])
  .controller('Ctrl', function ($filter) {
    var ctrl = this
    ctrl.html = {
      date: '29121986',
      cpf: '32165498700',
      cnpj: '11131821000183'
    }
    ctrl.js = {
      date: $filter('gumgaGenericFilter')('29121986', 'date'),
      cpf: $filter('gumgaGenericFilter')('32165498700', 'cpf'),
      cnpj: $filter('gumgaGenericFilter')('11131821000183', 'cnpj')
    }
  })

(function () {
  'use strict';

  require ('./form/form.js');
  require ('./form-class/form-class.js');
  require ('./errors/errors.js');
  require ('./error/error.js');
  require ('./max/date.js');
  require ('./max/length.js');
  require ('./max/number.js');
  require ('./min/date.js');
  require ('./min/length.js');
  require ('./min/number.js');
  require ('./pattern/pattern.js');
  require ('./range/date.js');
  require ('./range/number.js');
  require ('./required/required.js');
  require ('./validate/type.js');
  require ('./model-error/model-error.js');


  angular.module('gumga.form', [
    'gumga.form.form',
    'gumga.form.class',
    'gumga.form.errors',
    'gumga.form.error',
    'gumga.form.max.date',
    'gumga.form.max.length',
    'gumga.form.max.number',
    'gumga.form.min.date',
    'gumga.form.min.length',
    'gumga.form.min.number',
    'gumga.form.pattern',
    'gumga.form.range.date',
    'gumga.form.range.number',
    'gumga.form.required',
    'gumga.form.validate.type',
    'gumga.form.modelerror'
  ])
})();

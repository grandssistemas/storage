(function () {
  'use strict';

  Base.$inject = ['$http', '$q'];
  function Base($http, $q) {

    function RestPrototype(url, pageSize = 10) {
      this.pageSize = pageSize;
      this._url = url;
      this._query = { params: { start: 0, pageSize: this.pageSize } };
    }

    RestPrototype.prototype.get = _get;
    RestPrototype.prototype.resetAndGet = _resetAndGet;
    RestPrototype.prototype.getNew = _getNew;
    RestPrototype.prototype.getById = _getById;
    RestPrototype.prototype.save = _save;
    RestPrototype.prototype.update = _update;
    RestPrototype.prototype.delete = _delete;
    RestPrototype.prototype.sort = _sort;
    RestPrototype.prototype.deleteCollection = _deleteCollection;
    RestPrototype.prototype.saveImage = _saveImage;
    RestPrototype.prototype.deleteImage = _deleteImage;
    RestPrototype.prototype.getSearch = _getSearch;
    RestPrototype.prototype.getAdvancedSearch = _getAdvancedSearch;
    RestPrototype.prototype.resetDefaultState = _resetQuery;
    RestPrototype.prototype.getStateQuery = _getStateQuery;
    RestPrototype.prototype.redoSearch = _redoSearch;
    RestPrototype.prototype.saveQuery = _saveQuery;
    RestPrototype.prototype.getQuery = _getQuery;
    RestPrototype.prototype.postTags = _postTags;
    RestPrototype.prototype.getAvailableTags = _getAvailableTags;
    RestPrototype.prototype.getSelectedTags = _getSelectedTags;
    RestPrototype.prototype.extend = _extend;
    RestPrototype.prototype.getDocumentationURL = getDocumentationURL

    function _get(page, pageSize) {
      let query = angular.copy(this._query);
      query.params.pageSize = pageSize || query.params.pageSize;
      if (page) {
        query.params.start = (page - 1) * query.params.pageSize;
        if (page < 1) throw 'Invalid page';
      }
      return $http.get(this._url, query);
    }
    function _getNew() {
      return $http.get(this._url + '/new')
    }
    function _getById(id) {
      return $http.get(this._url + '/' + id);
    }
    function _save(v) {
      return $http.post(this._url, v);
    }
    function _update(v) {
      if (v.id) return $http.put(this._url + '/' + v.id, v);
      return this.save(v);
    }
    function _delete(v) {
      return $http.delete(this._url + '/' + v.id);
    }

    function _resetQuery() {
      this._query = {
        params: {
          start: 0,
          pageSize: this.pageSize
        }
      };
    }

    function _resetAndGet() {
      this.resetDefaultState();
      return $http.get(this._url, this._query);
    }
    function _sort(f, w, pageSize, page) {
      this.resetDefaultState();
      this._query.params.sortField = f;
      this._query.params.sortDir = w;
      if(pageSize){
        this._query.params.pageSize = pageSize;
      }
      if (page) {
        this._query.params.start = (page - 1) * this._query.params.pageSize
      }
      return $http.get(this._url, this._query);
    }
    function _deleteCollection(arr) {
      var url = this._url;
      return $q.all(arr.map(function (v) {
        return $http.delete(url + '/' + v.id);
      }))
    }
    function _saveImage(a, m) {
      var fd = new FormData();
      fd.append(a, m);
      return $http.post(this._url + '/' + a + '/', fd, {
        transformRequest: angular.identity,
        headers: { 'Content-Type': undefined }
      });
    }
    function _deleteImage(a) {
      var fd = new FormData();
      fd.append(a, {});
      return $http.delete(this._url + '/' + a, fd, {
        transformRequest: angular.identity,
        headers: { 'Content-Type': undefined }
      });
    }
    function _getSearch(f, p, pageSize, page) {
      this.resetDefaultState();
      (!p) ? p = '' : angular.noop;
      this._query.params.q = p;
      this._query.params.searchFields = f;
      if(pageSize){
        this._query.params.pageSize = pageSize;
      }

      return this.get(page);
    }
    function _getAdvancedSearch(p, pageSize, page) {
      this.resetDefaultState();
      if (typeof p === 'string') {
        this._query.params.aq = p;
        return $http.get(this._url, this._query);
      }
      this._query.params.aq = p.hql;
      this._query.params.aqo = JSON.stringify(p.source);
      if(pageSize){
        this._query.params.pageSize = pageSize;
      }
      var query = angular.copy(this._query);
      if (page) {
        query.params.start = (page - 1) * query.params.pageSize
      }
      return $http.get(this._url, query);
    }
    function _getStateQuery() {
      return this._query;
    }
    function _redoSearch() {
      return $http.get(this._url, this._query);
    }

    function _saveQuery(q) {
      var _aux = {
        page: location.hash.replace('#', '').replace(/\//gi, '_'),
        data: JSON.stringify(q.query),
        name: q.name
      };
      return $http.post(this._url + '/saq', _aux);
    }

    function _getQuery(page) {
      return $http.get(this._url + '/gumgauserdata/aq;' + page.replace('#', '').replace(/\//gi, '_'));
    }

    function _postTags(objectId, values = []) {
      var tags = [];
      values.forEach(function (v) {
        let newObj = angular.copy(v.definition);
        delete newObj.attributes
        tags.push({
          objectId: objectId, definition: newObj, values: v.definition.attributes.map(function (v) {
            let another = angular.copy(v);
            delete another.value;
            return {
              definition: another,
              value: v.value
            }
          })
        });
      });
      return $http.post(this._url + '/tags', { tags });
    }

    function _getAvailableTags() {
      return $http.get(`${this._url}/tags/`);
    }

    function _getSelectedTags(id) {
      return $http.get(`${this._url}/tags/${id}`);
    }

    function _extend(method = 'GET', urlExtended = ' ', params) {
      if (!$http[method.toLowerCase().trim()]) console.error('O método passado como primeiro parâmetro deve ser um método HTTP válido: GET, HEAD, POST, PUT, DELETE, JSONP, PATCH');
      return $http[method.toLowerCase().trim()](`${this._url}${urlExtended}`, params)
    }

    function getDocumentationURL() {
      let arrayUrl = this._url.split('')

      if (arrayUrl[arrayUrl.length - 1] == '/') {
        arrayUrl.pop()
      }

      const indexOfLastSlash = arrayUrl.join('').lastIndexOf('/')
      const urlWithoutSlash = arrayUrl.join('').slice(0, indexOfLastSlash)
      return urlWithoutSlash.concat('/proxy/softwarevalues')
    }

    return RestPrototype;
  }

  angular.module('gumga.rest', [])
    .service('GumgaRest', Base);

})();

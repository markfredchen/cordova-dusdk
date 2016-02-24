var exec = require('cordova/exec');

var BDLoc = {
  getLocation: function (successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'BDLocPlugin', 'getLocation', []);
  },
  startBackground: function (option, successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'BDLocPlugin', 'startBackground', [option]);
  },
  stopBackground: function (option, successCallback, errorCallback) {
    exec(successCallback, errorCallback, 'BDLocPlugin', 'stopBackground', []);
  },
  getBackgroundTrace: function(option, successCallback, errorCallback){
    exec(successCallback, errorCallback, 'BDLocPlugin', 'getBackgroundTrace', [option]);
  }
};

module.exports = BDLoc;

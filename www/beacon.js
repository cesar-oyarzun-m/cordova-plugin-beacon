/*global cordova, module*/

module.exports = {
    startScanning: function (name, successCallback, errorCallback) {
       cordova.exec(successCallback, errorCallback, "BeaconPlugin", "startScanning", [name]);
    }
};

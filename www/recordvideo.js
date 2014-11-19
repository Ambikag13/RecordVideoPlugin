var record = {
    createEvent: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'RecordVideo', // mapped to our native Java class called "CalendarPlugin"
            'recordVideo', // with this action name
            [{                  // and this array of custom arguments to create our entry
            }]
        ); 
    }
}
module.exports = record;
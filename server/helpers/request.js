var dbErrors = require('../helpers/dbInterface')('errors');

module.exports = {

	new: function(req, res) {
		return new Request(req, res);
	}

};

function Success(object) {
	this.success = true;
	for (var key in object)
		this[key] = object[key];
	this.toString = function() {
		return JSON.stringify(this);
	};
}
function Error(status, message, userID, intent) {
	dbErrors.put({
		status: status,
		message: message,
		userID: userID,
		intent: intent,
		occuredAt: Date.now()
	}, function(success, obj) {});
	this.success = false;
	this.status = status;
	this.message = message;
	this.toString = function() {
		return JSON.stringify(this)
	};
}

function Request(req, res) {
	this.body = returnBody(req);
	this.success = function(obj) {
		res.send(new Success(obj).toString());
	};
	this.error = function(status, message) {
		res.send(new Error(status, message).toString());
	}
}
function returnBody(req) {
	// TODO: include req.query in this
	// REMEMBER -> in get requests, all body field keys are lowercase
	if (req.method == 'POST') return req.body;
	else if (req.method == 'GET') return req.headers;
	else return req; // no change
}
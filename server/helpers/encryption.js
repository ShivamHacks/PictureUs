var crypto = require('crypto');
var config = require('../config');
var cryptoKey = config.cryptoKey;

module.exports = {

	encrypt: function(str) {
		try {
			var cipher = crypto.createCipher('aes-256-cbc', cryptoKey);
			cipher.update(str, 'utf8', 'base64');
			var encrypted = cipher.final('base64');
			return encrypted;
		} catch(err) { return 'error'; }
	},

	decrypt: function(str) {
		try {
			var decipher = crypto.createDecipher('aes-256-cbc', cryptoKey);
			decipher.update(str, 'base64', 'utf8');
			var decrypted = decipher.final('utf8');
			return decrypted;
		} catch(err) { return 'error'; }
	}
	
};
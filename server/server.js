var forever = require('forever-monitor');

var child = new (forever.Monitor)('app.js', {
	max: 3
});

child.on('restart', function() {
    console.error('Forever restarting script for ' + child.times + ' time');
});

child.on('exit:code', function(code) {
    console.error('Forever detected script exited with code ' + code);
});

child.on('exit', function () {
	console.log('APP HAS EXITED AFTER 3 ATTEMPTS');
});

child.start();
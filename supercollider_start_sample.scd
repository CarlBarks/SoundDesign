n = NetAddr("192.168.1.4", 57120);

a = 0;
b = 0;
c = 0;

(
~listener = {|msg, time, replyAddr, recvPort|
	if (msg[0] != "/status.reply", {
		// Log all received messages to the console.
		//("Message received on port" + recvPort + "from " + replyAddr.ip + ":" + replyAddr.port + ":" + msg).postln;
		("Message is " + msg).postln;
		a = msg[1];
		b = msg[2];
		c = msg[3];
	});
};

thisProcess.addOSCRecvFunc(~listener);
)
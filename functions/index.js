const functions = require("firebase-functions");
const admin = require('firebase-admin');

admin.initializeApp(); 

exports.sendNotification = functions.https.onRequest((req, res) => {
  const { to, senderName, messageContent, meetingId } = req.body;

  // Input validation
  if (!to || !senderName || !messageContent || !meetingId) {
    return res.status(400).send('Missing required data');
  }

  const message = {
    token: to,
    data: {
      senderName: senderName,
      messageContent: messageContent,
      meetingId: meetingId,
    },
  };

  admin.messaging().send(message)
    .then((response) => {
      console.log('Successfully sent message:', response);
      res.status(200).send('Notification sent successfully'); 
    })
    .catch((error) => {
      console.error('Error sending message:', error);
      res.status(500).send('Error sending notification'); 
    });
});
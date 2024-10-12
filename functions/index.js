/**
 * Import function triggers from their respective submodules:
 *
 * const {onCall} = require("firebase-functions/v2/https");
 * const {onDocumentWritten} = require("firebase-functions/v2/firestore");
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */

const {onRequest} = require("firebase-functions/v2/https");
const logger = require("firebase-functions/logger");

const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.updateFullName = functions.firestore
.document("users/{userId}")
.onUpdate(async (change, context) => {
    const newValue = change.after.data();
    const previousValue = change.before.data();

    if (newValue.firstName
        !== previousValue.firstName ||
        newValue.lastName !== previousValue.lastName) {
        const fullName = `${newValue.firstName} ${newValue.lastName}`;
    await change.after.ref.update({ fullName: fullName });
        }
});

exports.updateSlotsRemaining = functions.firestore
.document("meetings/{meetingId}")
.onUpdate(async (change, context) => {
    const newValue = change.after.data();
    const previousValue = change.before.data();

    if (newValue.participants.length !== previousValue.participants.length) {
        const slotsRemaining = newValue.slots - newValue.participants.length;
        await change.after.ref.update({ slotsRemaining: slotsRemaining });
    }
});

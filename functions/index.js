const functions = require("firebase-functions");
// The Firebase Admin SDK to access Firestore.
const serviceAccount = require("./serviceAccountKeyForQr.json");
const admin = require("firebase-admin");
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  databaseURL: "https://console.firebase.google.com/u/0/project/cloudfunctionpractice-405e5/firestore/data/~2Fusers~2FkG3KzrLALobZD4dUmuaQLFWW7Qo1.firebaseio.com"});
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions


exports.removeUser = functions.firestore.document("/users/{uid}")
    .onDelete((snapshot, context) => {
      const serviceAccount = require("./serviceAccountKeyForQr.json");
      if (!admin.apps.length) {
        admin.initializeApp({
          credential: admin.credential.cert(serviceAccount),
          databaseURL: "https://console.firebase.google.com/u/0/project/qrattendenceproject/firestore/data/~2Fusers~2FvdBzHAAktHMYo5UGHVOwS5cJper2.firebaseio.com",
        });
      }
      return admin.auth().deleteUser(context.params.uid)
          .then(function() {
            console.log("delete successful");
          }).catch(function(error) {
            console.log("ERROR ", error);
          });
    });

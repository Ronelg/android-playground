import { https } from "firebase-functions";

import { requestAccessToken } from "./endpoints/access_token";


export const getAccessToken = https.onCall((data, _) =>
  requestAccessToken(data.serverAuthCode)
);

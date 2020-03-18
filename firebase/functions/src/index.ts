import { https } from "firebase-functions";

import { Secrets } from "./secrets";
import { validString } from "./util";
import { requestAccessToken } from "./endpoints/access_token";

export const getAppSecretKey = https.onCall(
  (data, context) => Secrets.clientSecret
);

export const getAccessToken = https.onCall((data, _) =>
  requestAccessToken(data.serverAuthCode)
);

export const getRefreshToken = https.onCall((data, context) => {
  const { userId, serverAuthCode } = data;

  if (!validString(userId))
    throw new https.HttpsError(
      "invalid-argument",
      "Must be called with 'userId'"
    );

  if (!validString(serverAuthCode))
    throw new https.HttpsError(
      "invalid-argument",
      "Must be called with 'serverAuthCode'"
    );

  return "foobar";
});

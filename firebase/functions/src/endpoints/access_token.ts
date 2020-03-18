import { https } from "firebase-functions";

import { validString, validNumber } from "../util";
import { postRequestAuthToken } from "../client";

export interface RequestAccessTokenResult {
  accessToken: string;
  expiry: number;
}

export async function requestAccessToken(
  serverAuthCode: string
): Promise<RequestAccessTokenResult> {
  if (!validString(serverAuthCode))
    throw new https.HttpsError(
      "invalid-argument",
      "Must be called with 'serverAuthCode'"
    );

  try {
    const { access_token, expires_in } = await postRequestAuthToken(
      serverAuthCode
    );
    if (validString(access_token) && validNumber(expires_in)) {
      return {
        accessToken: access_token,
        expiry: expires_in
      };
    }

    console.error(`No valid access_token was found in response!`);
    throw new https.HttpsError(
      "unavailable",
      `Access token or expiry date was not valid!`
    );
  } catch (error) {
    console.error(`Unable to get accessToken using ${serverAuthCode}`);
    console.error(error);

    throw new https.HttpsError(
      "permission-denied",
      `Request for ${serverAuthCode} failed`
    );
  }
}

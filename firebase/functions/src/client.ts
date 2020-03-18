import axios from "axios";

import {
  AccessTokenRequest,
  createTokenRequest,
  AccessTokenResponse
} from "./types/access_token";

const GOOGLE_OAUTH_TOKEN_URL = "https://www.googleapis.com/oauth2/v4/token";

export async function postRequestAuthToken(
  serverAuthCode: string
): Promise<AccessTokenResponse> {
  const body: AccessTokenRequest = createTokenRequest(serverAuthCode);

  const result = await axios.post(GOOGLE_OAUTH_TOKEN_URL, body);
  return result.data;
}

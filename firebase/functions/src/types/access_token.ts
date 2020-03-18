import { Secrets } from "../secrets";

export interface AccessTokenRequest {
  grant_type: string;
  client_id: string;
  client_secret: string;
  redirect_uri: string | undefined;
  code: string;
}

export interface AccessTokenResponse {
  access_token: string;
  expires_in: number;
  scope: string;
  token_type: string;
  refresh_token: string | undefined;
}

export function createTokenRequest(serverAuthCode: string): AccessTokenRequest {
  return {
    grant_type: "authorization_code",
    client_id: Secrets.clientId,
    client_secret: Secrets.clientSecret,
    redirect_uri: "",
    code: serverAuthCode
  };
}

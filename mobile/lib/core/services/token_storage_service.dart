import 'package:shared_preferences/shared_preferences.dart';

class TokenStorageService {
  const TokenStorageService();

  static const _accessTokenKey = 'auth_access_token';
  static const _tokenTypeKey = 'auth_token_type';

  Future<void> saveToken({
    required String accessToken,
    String tokenType = 'Bearer',
  }) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_accessTokenKey, accessToken);
    await prefs.setString(_tokenTypeKey, tokenType);
  }

  Future<String?> getAccessToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_accessTokenKey);
  }

  Future<String?> getTokenType() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(_tokenTypeKey);
  }

  Future<String?> getAuthorizationHeader() async {
    final accessToken = await getAccessToken();
    if (accessToken == null || accessToken.trim().isEmpty) {
      return null;
    }

    final tokenType = await getTokenType();
    final normalizedTokenType =
        (tokenType == null || tokenType.trim().isEmpty) ? 'Bearer' : tokenType;

    return '$normalizedTokenType $accessToken';
  }

  Future<void> clearToken() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(_accessTokenKey);
    await prefs.remove(_tokenTypeKey);
  }
}

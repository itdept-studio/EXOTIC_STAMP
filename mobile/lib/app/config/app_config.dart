import 'package:flutter/services.dart';

class AppConfig {
  static const appName = 'Metro Stamp';
  static String _baseUrl = 'http://localhost:8080/api/v1';
  static const _apiPrefix = '/api/v1';

  static String get baseUrl => _baseUrl;

  static Future<void> loadFromEnv() async {
    try {
      final rawEnv = await rootBundle.loadString('.env');
      final values = _parseEnv(rawEnv);

      final apiBaseUrl = values['API_BASE_URL']?.trim() ?? '';
      final backendUrl = values['BACKEND_URL']?.trim() ?? '';

      if (apiBaseUrl.isNotEmpty) {
        _baseUrl = _resolveApiBaseUrl(apiBaseUrl);
        return;
      }

      if (backendUrl.isNotEmpty) {
        _baseUrl = '${_normalizeBaseUrl(backendUrl)}$_apiPrefix';
      }
    } catch (_) {
      // Keep default baseUrl when .env is missing or invalid.
    }
  }

  static Map<String, String> _parseEnv(String rawEnv) {
    final result = <String, String>{};
    for (final line in rawEnv.split('\n')) {
      final trimmed = line.trim();
      if (trimmed.isEmpty || trimmed.startsWith('#')) {
        continue;
      }

      final separatorIndex = trimmed.indexOf('=');
      if (separatorIndex <= 0) {
        continue;
      }

      final key = trimmed.substring(0, separatorIndex).trim();
      final value = trimmed.substring(separatorIndex + 1).trim();
      result[key] = value;
    }
    return result;
  }

  static String _normalizeBaseUrl(String value) {
    if (value.endsWith('/')) {
      return value.substring(0, value.length - 1);
    }
    return value;
  }

  static String _resolveApiBaseUrl(String value) {
    final normalized = _normalizeBaseUrl(value);

    if (normalized.endsWith(_apiPrefix)) {
      return normalized;
    }

    return '$normalized$_apiPrefix';
  }
}

import 'dart:convert';
import 'dart:io';

import '../../app/config/app_config.dart';
import '../errors/app_exception.dart';

class ApiClient {
  static final HttpClient _sharedHttpClient = HttpClient();

  ApiClient({HttpClient? httpClient})
      : _httpClient = httpClient ?? _sharedHttpClient;

  final HttpClient _httpClient;

  Future<ApiHttpResponse> postJson({
    required String path,
    required Map<String, dynamic> body,
    Map<String, String> headers = const {},
  }) async {
    final uri = _buildUri(path);

    try {
      final request = await _httpClient.postUrl(uri);
      request.headers.set(HttpHeaders.contentTypeHeader, 'application/json');
      headers.forEach(request.headers.set);
      request.write(jsonEncode(body));

      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      final decodedBody = _decodeResponseBody(responseBody);

      return ApiHttpResponse(
        statusCode: response.statusCode,
        body: decodedBody,
        rawBody: responseBody,
      );
    } on SocketException {
      throw AppException('Không thể kết nối đến máy chủ. Vui lòng thử lại.');
    } on HttpException {
      throw AppException('Kết nối máy chủ thất bại. Vui lòng thử lại.');
    } on FormatException {
      throw AppException('Dữ liệu phản hồi không hợp lệ từ máy chủ.');
    }
  }

  Future<ApiHttpResponse> getJson({
    required String path,
    Map<String, String> headers = const {},
  }) async {
    final uri = _buildUri(path);

    try {
      final request = await _httpClient.getUrl(uri);
      headers.forEach(request.headers.set);

      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      final decodedBody = _decodeResponseBody(responseBody);

      return ApiHttpResponse(
        statusCode: response.statusCode,
        body: decodedBody,
        rawBody: responseBody,
      );
    } on SocketException {
      throw AppException('Không thể kết nối đến máy chủ. Vui lòng thử lại.');
    } on HttpException {
      throw AppException('Kết nối máy chủ thất bại. Vui lòng thử lại.');
    } on FormatException {
      throw AppException('Dữ liệu phản hồi không hợp lệ từ máy chủ.');
    }
  }

  Future<ApiHttpResponse> putJson({
    required String path,
    required Map<String, dynamic> body,
    Map<String, String> headers = const {},
  }) async {
    final uri = _buildUri(path);

    try {
      final request = await _httpClient.putUrl(uri);
      request.headers.set(HttpHeaders.contentTypeHeader, 'application/json');
      headers.forEach(request.headers.set);
      request.write(jsonEncode(body));

      final response = await request.close();
      final responseBody = await response.transform(utf8.decoder).join();
      final decodedBody = _decodeResponseBody(responseBody);

      return ApiHttpResponse(
        statusCode: response.statusCode,
        body: decodedBody,
        rawBody: responseBody,
      );
    } on SocketException {
      throw AppException('Không thể kết nối đến máy chủ. Vui lòng thử lại.');
    } on HttpException {
      throw AppException('Kết nối máy chủ thất bại. Vui lòng thử lại.');
    } on FormatException {
      throw AppException('Dữ liệu phản hồi không hợp lệ từ máy chủ.');
    }
  }

  Uri _buildUri(String path) {
    final base = AppConfig.baseUrl.endsWith('/')
        ? AppConfig.baseUrl.substring(0, AppConfig.baseUrl.length - 1)
        : AppConfig.baseUrl;
    final normalizedPath = path.startsWith('/') ? path : '/$path';
    return Uri.parse('$base$normalizedPath');
  }

  dynamic _decodeResponseBody(String responseBody) {
    final trimmedBody = responseBody.trim();
    if (trimmedBody.isEmpty) {
      return null;
    }

    try {
      return jsonDecode(trimmedBody);
    } on FormatException {
      return trimmedBody;
    }
  }
}

class ApiHttpResponse {
  const ApiHttpResponse({
    required this.statusCode,
    required this.body,
    required this.rawBody,
  });

  final int statusCode;
  final dynamic body;
  final String rawBody;

  bool get isSuccess => statusCode >= 200 && statusCode < 300;
}

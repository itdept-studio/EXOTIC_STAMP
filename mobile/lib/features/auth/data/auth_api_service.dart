import '../../../core/errors/app_exception.dart';
import '../../../core/services/api_client.dart';

class AuthApiService {
  AuthApiService({ApiClient? apiClient})
      : _apiClient = apiClient ?? ApiClient();

  final ApiClient _apiClient;

  Future<void> register({
    required String firstName,
    required String lastName,
    required String username,
    required String email,
    required String phoneNumber,
    required String password,
  }) async {
    final response = await _apiClient.postJson(
      path: '/auth/register',
      body: {
        'firstname': firstName,
        'lastname': lastName,
        'username': username,
        'email': email,
        'phoneNumber': phoneNumber,
        'password': password,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }
  }

  Future<void> verifyEmail({
    required String email,
    required String otp,
  }) async {
    final response = await _apiClient.postJson(
      path: '/auth/verify-email',
      body: {
        'email': email,
        'otp': otp,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }
  }

  Future<void> resendVerification({
    required String email,
  }) async {
    final response = await _apiClient.postJson(
      path: '/auth/resend-verification',
      body: {
        'email': email,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }
  }

  Future<AuthLoginResult> login({
    required String identifier,
    required String password,
    required String deviceFingerprint,
  }) async {
    final response = await _apiClient.postJson(
      path: '/auth/login',
      body: {
        'identifier': identifier,
        'password': password,
        'deviceFingerprint': deviceFingerprint,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }

    return _parseLoginResponse(response.body);
  }

  Future<AuthLoginResult> refreshAccessToken() async {
    final response = await _apiClient.postJson(
      path: '/auth/refresh',
      body: const <String, dynamic>{},
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }

    return _parseLoginResponse(response.body);
  }

  Future<void> logout({
    required String authorizationHeader,
  }) async {
    final response = await _apiClient.postJson(
      path: '/auth/logout',
      body: const <String, dynamic>{},
      headers: {
        'Authorization': authorizationHeader,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }
  }

  Future<void> forgotPassword({
    required String email,
  }) async {
    final response = await _apiClient.postJson(
      path: '/auth/forgot-password',
      body: {
        'email': email,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }
  }

  Future<void> resendForgotPasswordOtp({
    required String email,
  }) async {
    final response = await _apiClient.postJson(
      path: '/auth/resend-otp',
      body: {
        'email': email,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }
  }

  Future<void> resetPassword({
    required String email,
    required String otp,
    required String newPassword,
  }) async {
    final response = await _apiClient.postJson(
      path: '/auth/reset-password',
      body: {
        'email': email,
        'otp': otp,
        'newPassword': newPassword,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }
  }

  Future<CurrentUserProfile> getCurrentUser({
    required String authorizationHeader,
  }) async {
    final response = await _apiClient.getJson(
      path: '/users/me',
      headers: {
        'Authorization': authorizationHeader,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }

    final body = response.body;
    if (body is! Map<String, dynamic>) {
      return const CurrentUserProfile();
    }

    final firstName = body['firstname'];
    final lastName = body['lastname'];
    final username = body['username'];
    final email = body['email'];
    final phoneNumber = body['phoneNumber'];
    final dob = body['dob'];
    final gender = body['gender'];
    final bio = body['bio'];
    final status = body['status'];
    final createdAt = body['created_at'];
    final id = body['id'];
    final avatarUrl = body['avatarUrl'];

    return CurrentUserProfile(
      firstName: firstName is String && firstName.trim().isNotEmpty
          ? firstName.trim()
          : null,
      lastName: lastName is String && lastName.trim().isNotEmpty
          ? lastName.trim()
          : null,
      username: username is String && username.trim().isNotEmpty
          ? username.trim()
          : null,
      email: email is String && email.trim().isNotEmpty ? email.trim() : null,
      phoneNumber: phoneNumber is String && phoneNumber.trim().isNotEmpty
          ? phoneNumber.trim()
          : null,
      dob: dob is String && dob.trim().isNotEmpty ? dob.trim() : null,
      gender: gender is bool ? gender : null,
      bio: bio is String && bio.trim().isNotEmpty ? bio.trim() : null,
      status:
          status is String && status.trim().isNotEmpty ? status.trim() : null,
      createdAt: createdAt is String && createdAt.trim().isNotEmpty
          ? createdAt.trim()
          : null,
      id: id is String && id.trim().isNotEmpty ? id.trim() : null,
      avatarUrl:
          avatarUrl is String && avatarUrl.trim().isNotEmpty ? avatarUrl : null,
    );
  }

  Future<CurrentUserProfile> updateUserProfile({
    required String authorizationHeader,
    required String userId,
    required String firstName,
    required String lastName,
    required String bio,
    required String avatarUrl,
    required bool gender,
    required String dob,
  }) async {
    final response = await _apiClient.putJson(
      path: '/users/$userId',
      headers: {
        'Authorization': authorizationHeader,
      },
      body: {
        'firstname': firstName,
        'lastname': lastName,
        'bio': bio,
        'avatarUrl': avatarUrl,
        'gender': gender,
        'dob': dob,
      },
    );

    if (!response.isSuccess) {
      throw AppException(_extractErrorMessage(response.body));
    }

    final body = response.body;
    if (body is! Map<String, dynamic>) {
      return const CurrentUserProfile();
    }

    final firstNameValue = body['firstname'];
    final lastNameValue = body['lastname'];
    final username = body['username'];
    final email = body['email'];
    final phoneNumber = body['phoneNumber'];
    final dobValue = body['dob'];
    final genderValue = body['gender'];
    final bioValue = body['bio'];
    final status = body['status'];
    final createdAt = body['created_at'];
    final id = body['id'];
    final avatarUrlValue = body['avatarUrl'];

    return CurrentUserProfile(
      firstName: firstNameValue is String && firstNameValue.trim().isNotEmpty
          ? firstNameValue.trim()
          : null,
      lastName: lastNameValue is String && lastNameValue.trim().isNotEmpty
          ? lastNameValue.trim()
          : null,
      username: username is String && username.trim().isNotEmpty
          ? username.trim()
          : null,
      email: email is String && email.trim().isNotEmpty ? email.trim() : null,
      phoneNumber: phoneNumber is String && phoneNumber.trim().isNotEmpty
          ? phoneNumber.trim()
          : null,
      dob: dobValue is String && dobValue.trim().isNotEmpty
          ? dobValue.trim()
          : null,
      gender: genderValue is bool ? genderValue : null,
      bio: bioValue is String && bioValue.trim().isNotEmpty
          ? bioValue.trim()
          : null,
      status:
          status is String && status.trim().isNotEmpty ? status.trim() : null,
      createdAt: createdAt is String && createdAt.trim().isNotEmpty
          ? createdAt.trim()
          : null,
      id: id is String && id.trim().isNotEmpty ? id.trim() : null,
      avatarUrl: avatarUrlValue is String && avatarUrlValue.trim().isNotEmpty
          ? avatarUrlValue.trim()
          : null,
    );
  }

  String _extractErrorMessage(dynamic responseBody) {
    if (responseBody is String && responseBody.trim().isNotEmpty) {
      return responseBody.trim();
    }

    if (responseBody is Map<String, dynamic>) {
      final message = responseBody['message'];
      if (message is String && message.trim().isNotEmpty) {
        return message;
      }

      final error = responseBody['error'];
      if (error is String && error.trim().isNotEmpty) {
        return error;
      }
    }

    return 'Đăng ký chưa thành công. Vui lòng thử lại.';
  }

  AuthLoginResult _parseLoginResponse(dynamic responseBody) {
    if (responseBody is! Map<String, dynamic>) {
      return const AuthLoginResult();
    }

    final accessToken = responseBody['accessToken'];
    final tokenType = responseBody['tokenType'];
    final userInfo = responseBody['userInfo'];
    final rawRoles =
        userInfo is Map<String, dynamic> ? userInfo['roles'] : null;
    final roles = rawRoles is List
        ? rawRoles
            .whereType<String>()
            .map((item) => item.trim())
            .where((item) => item.isNotEmpty)
            .toList()
        : const <String>[];

    return AuthLoginResult(
      accessToken:
          accessToken is String && accessToken.isNotEmpty ? accessToken : null,
      tokenType: tokenType is String && tokenType.isNotEmpty ? tokenType : null,
      userEmail: userInfo is Map<String, dynamic>
          ? userInfo['email'] as String?
          : null,
      username: userInfo is Map<String, dynamic>
          ? userInfo['username'] as String?
          : null,
      roles: roles,
    );
  }
}

class AuthLoginResult {
  const AuthLoginResult({
    this.accessToken,
    this.tokenType,
    this.userEmail,
    this.username,
    this.roles = const [],
  });

  final String? accessToken;
  final String? tokenType;
  final String? userEmail;
  final String? username;
  final List<String> roles;
}

class CurrentUserProfile {
  const CurrentUserProfile({
    this.id,
    this.firstName,
    this.lastName,
    this.username,
    this.email,
    this.phoneNumber,
    this.dob,
    this.gender,
    this.bio,
    this.status,
    this.createdAt,
    this.avatarUrl,
  });

  final String? id;
  final String? firstName;
  final String? lastName;
  final String? username;
  final String? email;
  final String? phoneNumber;
  final String? dob;
  final bool? gender;
  final String? bio;
  final String? status;
  final String? createdAt;
  final String? avatarUrl;

  String get displayName {
    final fullName = [
      if (firstName != null && firstName!.trim().isNotEmpty) firstName!.trim(),
      if (lastName != null && lastName!.trim().isNotEmpty) lastName!.trim(),
    ].join(' ');

    if (fullName.isNotEmpty) {
      return fullName;
    }

    if (username != null && username!.trim().isNotEmpty) {
      return username!.trim();
    }

    if (email != null && email!.trim().isNotEmpty) {
      return email!.trim();
    }

    return 'Người dùng';
  }
}

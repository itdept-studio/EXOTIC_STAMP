import 'package:flutter/material.dart';

import '../features/auth/presentation/auth_page.dart';
import '../features/home/home_page.dart';
import '../features/profile/profile_page.dart';
import '../features/rewards/rewards_page.dart';
import '../features/scan/scan_page.dart';
import '../features/stamp_book/stamp_book_page.dart';
import '../features/station/station_page.dart';

class AppRouter {
  static const home = '/';
  static const auth = '/auth';
  static const stations = '/stations';
  static const scan = '/scan';
  static const stampBook = '/stamp-book';
  static const rewards = '/rewards';
  static const profile = '/profile';

  static Route<dynamic> onGenerateRoute(RouteSettings settings) {
    switch (settings.name) {
      case auth:
        return MaterialPageRoute(builder: (_) => const AuthPage());
      case stations:
        return MaterialPageRoute(builder: (_) => const StationPage());
      case scan:
        return MaterialPageRoute(builder: (_) => const ScanPage());
      case stampBook:
        return MaterialPageRoute(builder: (_) => const StampBookPage());
      case rewards:
        return MaterialPageRoute(builder: (_) => const RewardsPage());
      case profile:
        return MaterialPageRoute(builder: (_) => const ProfilePage());
      case home:
      default:
        return MaterialPageRoute(builder: (_) => const HomePage());
    }
  }
}


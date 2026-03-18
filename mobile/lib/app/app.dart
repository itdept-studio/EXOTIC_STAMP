import 'package:flutter/material.dart';

import 'router.dart';
import 'theme/app_theme.dart';

class MetroStampApp extends StatelessWidget {
  const MetroStampApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Metro Stamp',
      theme: AppTheme.light(),
      onGenerateRoute: AppRouter.onGenerateRoute,
      initialRoute: AppRouter.home,
    );
  }
}


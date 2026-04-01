import 'dart:io';

import 'package:flutter/material.dart';
import 'package:geolocator/geolocator.dart';
import 'package:url_launcher/url_launcher.dart';

import '../../app/router.dart';
import '../../app/theme/app_colors.dart';
import 'data/station_catalog.dart';

class StationPage extends StatefulWidget {
  const StationPage({super.key});

  @override
  State<StationPage> createState() => _StationPageState();
}

class _StationPageState extends State<StationPage> {
  final TextEditingController searchController = TextEditingController();

  int selectedLineIndex = 0;
  Position? _currentPosition;
  bool _isResolvingLocation = false;
  String? _locationStatus;

  static const List<String> _lineFilters = [
    'All Lines',
    'Line 1',
    'Line 2',
    'Line 5',
  ];

  @override
  void initState() {
    super.initState();
    _resolveCurrentLocation();
  }

  void _openStationDetail(StationItem station) {
    Navigator.of(context).push(
      MaterialPageRoute(
        builder: (_) => _StationDetailPage(
          detail: _StationDetailData.fromStation(station),
          onOpenDirections: () => _openDirections(station),
        ),
      ),
    );
  }

  Future<void> _resolveCurrentLocation() async {
    if (_isResolvingLocation) {
      return;
    }

    setState(() {
      _isResolvingLocation = true;
      _locationStatus = null;
    });

    try {
      final serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        _setLocationStatus(
            'Hãy bật Location Services để sắp xếp ga chính xác.');
        return;
      }

      var permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
      }

      if (permission == LocationPermission.denied) {
        _setLocationStatus('Bạn chưa cấp quyền vị trí cho ứng dụng.');
        return;
      }

      if (permission == LocationPermission.deniedForever) {
        _setLocationStatus(
          'Quyền vị trí đã bị chặn vĩnh viễn. Hãy bật lại trong Settings.',
        );
        return;
      }

      final position = await Geolocator.getCurrentPosition(
        locationSettings: const LocationSettings(
          accuracy: LocationAccuracy.high,
        ),
      );

      if (!mounted) {
        return;
      }

      setState(() {
        _currentPosition = position;
      });
    } catch (_) {
      _setLocationStatus('Không lấy được vị trí hiện tại. Vui lòng thử lại.');
    } finally {
      if (mounted) {
        setState(() {
          _isResolvingLocation = false;
        });
      }
    }
  }

  void _setLocationStatus(String message) {
    if (!mounted) {
      return;
    }

    setState(() {
      _locationStatus = message;
    });
  }

  double _distanceFor(StationItem station) {
    final position = _currentPosition;
    if (position == null) {
      return station.distanceKm;
    }

    final distanceInMeters = Geolocator.distanceBetween(
      position.latitude,
      position.longitude,
      station.latitude,
      station.longitude,
    );
    return distanceInMeters / 1000;
  }

  String _distanceLabel(StationItem station) {
    final distanceKm = _distanceFor(station);
    if (distanceKm < 1) {
      return '${(distanceKm * 1000).round()} m';
    }
    return '${distanceKm.toStringAsFixed(1)} km';
  }

  StationItem get _nearestStation {
    final sortedStations = [...stationCatalog]
      ..sort((a, b) => _distanceFor(a).compareTo(_distanceFor(b)));
    return sortedStations.first;
  }

  String get _nearestStationSubtitle {
    final isLiveDistance = _currentPosition != null;
    final prefix = isLiveDistance
        ? '${_distanceLabel(_nearestStation)} away'
        : 'Approx. ${_distanceLabel(_nearestStation)} away';
    return '$prefix • ${_nearestStation.line}';
  }

  Future<void> _openDirections(StationItem station) async {
    final destination = '${station.latitude},${station.longitude}';
    final currentPosition = _currentPosition;
    final currentDistanceKm =
        currentPosition == null ? null : _distanceFor(station);
    final shouldIncludeOrigin = currentPosition != null &&
        currentDistanceKm != null &&
        currentDistanceKm <= 200;
    final origin = shouldIncludeOrigin
        ? '${currentPosition.latitude},${currentPosition.longitude}'
        : null;
    final webDirectionsUri = Uri.parse(
      'https://www.google.com/maps/dir/?api=1&destination=$destination${origin == null ? '' : '&origin=$origin'}${origin == null ? '' : '&travelmode=walking'}',
    );
    final webPlaceUri = Uri.parse(
      'https://www.google.com/maps/search/?api=1&query=$destination',
    );

    final candidateUris = <Uri>[
      if (Platform.isAndroid)
        Uri.parse('google.navigation:q=$destination&mode=w'),
      if (Platform.isIOS)
        Uri.parse(
          'comgooglemaps://?daddr=$destination&directionsmode=walking',
        ),
      webDirectionsUri,
      webPlaceUri,
    ];

    for (final uri in candidateUris) {
      if (await canLaunchUrl(uri)) {
        await launchUrl(uri, mode: LaunchMode.externalApplication);
        return;
      }
    }

    if (!mounted) {
      return;
    }

    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(
          content: Text('Không mở được Google Maps trên thiết bị này.')),
    );
  }

  List<StationItem> get _visibleStations {
    final query = searchController.text.trim().toLowerCase();
    final selectedLine = _lineFilters[selectedLineIndex];

    return stationCatalog.where((station) {
      final matchesLine =
          selectedLine == 'All Lines' || station.line == selectedLine;
      final matchesQuery =
          query.isEmpty || station.name.toLowerCase().contains(query);
      return matchesLine && matchesQuery;
    }).toList()
      ..sort((a, b) => _distanceFor(a).compareTo(_distanceFor(b)));
  }

  @override
  void dispose() {
    searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final visibleStations = _visibleStations;
    final nearestStation = _nearestStation;

    return Scaffold(
      backgroundColor: AppColors.background,
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      floatingActionButton: Transform.translate(
        offset: const Offset(0, 12),
        child: Container(
          width: 78,
          height: 78,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: AppColors.background,
            boxShadow: [
              BoxShadow(
                color: AppColors.brandRed.withValues(alpha: 0.26),
                blurRadius: 22,
                offset: const Offset(0, 10),
              ),
            ],
          ),
          child: Padding(
            padding: const EdgeInsets.all(6),
            child: FloatingActionButton(
              elevation: 0,
              backgroundColor: AppColors.brandRed,
              foregroundColor: AppColors.background,
              onPressed: () => Navigator.of(context).pushNamed(AppRouter.scan),
              shape: const CircleBorder(),
              child: const Icon(Icons.center_focus_strong_rounded, size: 30),
            ),
          ),
        ),
      ),
      bottomNavigationBar: _StationsBottomBar(
        selectedIndex: 2,
        onSelected: (index) {
          switch (index) {
            case 0:
              Navigator.of(context).pushReplacementNamed(AppRouter.home);
              break;
            case 1:
              Navigator.of(context).pushReplacementNamed(AppRouter.stampBook);
              break;
            case 2:
              break;
            case 3:
              Navigator.of(context).pushReplacementNamed(AppRouter.profile);
              break;
          }
        },
      ),
      body: SafeArea(
        bottom: false,
        child: Column(
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(20, 18, 20, 18),
              child: Row(
                children: [
                  const Expanded(
                    child: Text(
                      'Stations',
                      style: TextStyle(
                        fontSize: 24,
                        fontWeight: FontWeight.w800,
                        color: AppColors.brandBlue,
                      ),
                    ),
                  ),
                  IconButton(
                    onPressed: () {},
                    icon: const Icon(
                      Icons.filter_alt_outlined,
                      color: AppColors.brandRed,
                      size: 28,
                    ),
                  ),
                ],
              ),
            ),
            const Divider(height: 1, thickness: 1, color: Color(0xFFE5E9F0)),
            Expanded(
              child: ListView(
                padding: const EdgeInsets.fromLTRB(20, 20, 20, 110),
                children: [
                  DecoratedBox(
                    decoration: BoxDecoration(
                      color: const Color(0xFFF7F9FC),
                      borderRadius: BorderRadius.circular(22),
                      boxShadow: const [
                        BoxShadow(
                          color: AppColors.shadow,
                          blurRadius: 16,
                          offset: Offset(0, 6),
                        ),
                      ],
                    ),
                    child: TextField(
                      controller: searchController,
                      onChanged: (_) => setState(() {}),
                      decoration: const InputDecoration(
                        hintText: 'Find a station...',
                        prefixIcon: Icon(
                          Icons.search_rounded,
                          color: AppColors.textPrimary,
                          size: 30,
                        ),
                        border: InputBorder.none,
                        contentPadding: EdgeInsets.symmetric(
                          horizontal: 20,
                          vertical: 20,
                        ),
                      ),
                      style: const TextStyle(
                        fontSize: 18,
                        fontWeight: FontWeight.w600,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ),
                  const SizedBox(height: 18),
                  SizedBox(
                    height: 48,
                    child: ListView.separated(
                      scrollDirection: Axis.horizontal,
                      itemCount: _lineFilters.length,
                      separatorBuilder: (_, index) => const SizedBox(width: 12),
                      itemBuilder: (context, index) {
                        final isSelected = index == selectedLineIndex;
                        return GestureDetector(
                          onTap: () {
                            setState(() {
                              selectedLineIndex = index;
                            });
                          },
                          child: AnimatedContainer(
                            duration: const Duration(milliseconds: 180),
                            padding: const EdgeInsets.symmetric(horizontal: 22),
                            decoration: BoxDecoration(
                              color: isSelected
                                  ? AppColors.brandBlue
                                  : AppColors.background,
                              borderRadius: BorderRadius.circular(999),
                              border: Border.all(
                                color: isSelected
                                    ? AppColors.brandBlue
                                    : const Color(0xFFD8DEE8),
                              ),
                            ),
                            alignment: Alignment.center,
                            child: Text(
                              _lineFilters[index],
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w800,
                                color: isSelected
                                    ? AppColors.background
                                    : AppColors.brandBlue,
                              ),
                            ),
                          ),
                        );
                      },
                    ),
                  ),
                  const SizedBox(height: 26),
                  Row(
                    children: [
                      const Expanded(
                        child: Text(
                          'Nearby Stations',
                          style: TextStyle(
                            fontSize: 24,
                            fontWeight: FontWeight.w800,
                            color: AppColors.textPrimary,
                          ),
                        ),
                      ),
                      TextButton(
                        onPressed: () {},
                        child: const Text(
                          'View Map',
                          style: TextStyle(
                            fontSize: 15,
                            fontWeight: FontWeight.w800,
                            color: AppColors.brandBlue,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 14),
                  if (_locationStatus != null || _isResolvingLocation) ...[
                    _LocationStatusCard(
                      message: _locationStatus ?? 'Đang lấy vị trí hiện tại...',
                      isLoading: _isResolvingLocation,
                      onRefresh:
                          _isResolvingLocation ? null : _resolveCurrentLocation,
                    ),
                    const SizedBox(height: 14),
                  ],
                  _NearestStationCard(
                    station: nearestStation,
                    subtitle: _nearestStationSubtitle,
                    onOpenDetail: () => _openStationDetail(nearestStation),
                    onOpenDirections: () => _openDirections(nearestStation),
                  ),
                  const SizedBox(height: 26),
                  Row(
                    children: [
                      const Expanded(
                        child: Text(
                          'Station Directory',
                          style: TextStyle(
                            fontSize: 22,
                            fontWeight: FontWeight.w800,
                            color: AppColors.textPrimary,
                          ),
                        ),
                      ),
                      TextButton.icon(
                        onPressed: () {},
                        iconAlignment: IconAlignment.end,
                        style: TextButton.styleFrom(
                          foregroundColor: AppColors.textPrimary,
                        ),
                        icon: const Icon(Icons.keyboard_arrow_down_rounded),
                        label: const Text(
                          'Sorted by distance',
                          style: TextStyle(
                            fontSize: 14,
                            fontWeight: FontWeight.w700,
                          ),
                        ),
                      ),
                    ],
                  ),
                  const SizedBox(height: 10),
                  for (final station in visibleStations) ...[
                    _StationListCard(
                      station: station,
                      distanceLabel: _distanceLabel(station),
                      onTap: () => _openStationDetail(station),
                    ),
                    const SizedBox(height: 16),
                  ],
                  Text(
                    visibleStations.isEmpty
                        ? 'No stations match this filter'
                        : "You've reached the end of ${_lineFilters[selectedLineIndex]}",
                    textAlign: TextAlign.center,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w500,
                      fontStyle: FontStyle.italic,
                      color: AppColors.brandRed,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _LocationStatusCard extends StatelessWidget {
  const _LocationStatusCard({
    required this.message,
    required this.isLoading,
    this.onRefresh,
  });

  final String message;
  final bool isLoading;
  final VoidCallback? onRefresh;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
      decoration: BoxDecoration(
        color: const Color(0xFFF6FAFF),
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: const Color(0xFFD6E6FA)),
      ),
      child: Row(
        children: [
          if (isLoading)
            const SizedBox(
              width: 18,
              height: 18,
              child: CircularProgressIndicator(strokeWidth: 2.2),
            )
          else
            const Icon(
              Icons.my_location_rounded,
              color: AppColors.brandBlue,
              size: 20,
            ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              message,
              style: const TextStyle(
                fontSize: 13,
                fontWeight: FontWeight.w700,
                color: AppColors.textPrimary,
              ),
            ),
          ),
          if (onRefresh != null)
            TextButton(
              onPressed: onRefresh,
              child: const Text(
                'Retry',
                style: TextStyle(fontWeight: FontWeight.w800),
              ),
            ),
        ],
      ),
    );
  }
}

class _NearestStationCard extends StatelessWidget {
  const _NearestStationCard({
    required this.station,
    required this.subtitle,
    required this.onOpenDetail,
    required this.onOpenDirections,
  });

  final StationItem station;
  final String subtitle;
  final VoidCallback onOpenDetail;
  final VoidCallback onOpenDirections;

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 168,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(28),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 24,
            offset: Offset(0, 12),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(28),
        child: Stack(
          fit: StackFit.expand,
          children: [
            _StationImage(path: station.heroImagePath),
            DecoratedBox(
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  begin: Alignment.topCenter,
                  end: Alignment.bottomCenter,
                  colors: [
                    Colors.black.withValues(alpha: 0.10),
                    Colors.black.withValues(alpha: 0.18),
                    Colors.black.withValues(alpha: 0.52),
                  ],
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(
                      horizontal: 12,
                      vertical: 2,
                    ),
                    decoration: BoxDecoration(
                      color: AppColors.brandBlue,
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: const Text(
                      'Nearest Station',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w800,
                        color: AppColors.background,
                      ),
                    ),
                  ),
                  const Spacer(),
                  Row(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Expanded(
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            Text(
                              station.name,
                              style: const TextStyle(
                                fontSize: 22,
                                fontWeight: FontWeight.w900,
                                color: AppColors.background,
                              ),
                            ),
                            const SizedBox(height: 8),
                            Text(
                              subtitle,
                              style: const TextStyle(
                                fontSize: 13,
                                fontWeight: FontWeight.w600,
                                color: AppColors.background,
                              ),
                            ),
                          ],
                        ),
                      ),
                      const SizedBox(width: 16),
                      Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          _HeroActionButton(
                            icon: Icons.info_outline_rounded,
                            onTap: onOpenDetail,
                          ),
                          const SizedBox(height: 10),
                          _HeroActionButton(
                            icon: Icons.near_me_rounded,
                            onTap: onOpenDirections,
                          ),
                        ],
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _HeroActionButton extends StatelessWidget {
  const _HeroActionButton({required this.icon, required this.onTap});

  final IconData icon;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: AppColors.background,
      shape: const CircleBorder(),
      child: InkWell(
        onTap: onTap,
        customBorder: const CircleBorder(),
        child: SizedBox(
          width: 46,
          height: 46,
          child: Icon(
            icon,
            color: AppColors.brandBlue,
            size: 24,
          ),
        ),
      ),
    );
  }
}

class _StationListCard extends StatelessWidget {
  const _StationListCard({
    required this.station,
    required this.distanceLabel,
    required this.onTap,
  });

  final StationItem station;
  final String distanceLabel;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(24),
        child: Ink(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: AppColors.background,
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: const Color(0xFFDCE2EB)),
            boxShadow: const [
              BoxShadow(
                color: AppColors.shadow,
                blurRadius: 18,
                offset: Offset(0, 8),
              ),
            ],
          ),
          child: Row(
            children: [
              Stack(
                clipBehavior: Clip.none,
                children: [
                  ClipOval(
                    child: SizedBox(
                      width: 82,
                      height: 82,
                      child: _StationImage(path: station.imagePath),
                    ),
                  ),
                  Positioned(
                    right: -2,
                    bottom: -2,
                    child: Container(
                      width: 28,
                      height: 28,
                      decoration: BoxDecoration(
                        color: AppColors.background,
                        shape: BoxShape.circle,
                        border: Border.all(color: const Color(0xFFDCE2EB)),
                      ),
                      child: Icon(
                        station.isCollected
                            ? Icons.check_rounded
                            : Icons.circle_outlined,
                        size: 16,
                        color: station.isCollected
                            ? AppColors.textPrimary
                            : AppColors.textMuted,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      station.name,
                      style: const TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                      ),
                      maxLines: 2,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 8),
                    Wrap(
                      spacing: 12,
                      runSpacing: 8,
                      crossAxisAlignment: WrapCrossAlignment.center,
                      children: [
                        Container(
                          padding: const EdgeInsets.symmetric(
                            horizontal: 12,
                            vertical: 6,
                          ),
                          decoration: BoxDecoration(
                            color: const Color(0xFFE6F3FF),
                            borderRadius: BorderRadius.circular(10),
                          ),
                          child: Text(
                            station.line.toUpperCase(),
                            style: const TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.w800,
                              color: AppColors.brandBlue,
                            ),
                          ),
                        ),
                        Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const Icon(
                              Icons.schedule_rounded,
                              size: 20,
                              color: AppColors.textPrimary,
                            ),
                            const SizedBox(width: 6),
                            Text(
                              distanceLabel,
                              style: const TextStyle(
                                fontSize: 15,
                                fontWeight: FontWeight.w500,
                                color: AppColors.textPrimary,
                              ),
                              maxLines: 1,
                              overflow: TextOverflow.ellipsis,
                            ),
                          ],
                        ),
                      ],
                    ),
                  ],
                ),
              ),
              const SizedBox(width: 10),
              const Align(
                alignment: Alignment.center,
                child: Icon(
                  Icons.chevron_right_rounded,
                  size: 34,
                  color: Color(0xFF99A1AE),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _StationDetailPage extends StatelessWidget {
  const _StationDetailPage({
    required this.detail,
    required this.onOpenDirections,
  });

  final _StationDetailData detail;
  final VoidCallback onOpenDirections;

  @override
  Widget build(BuildContext context) {
    const headerHeight = 430.0;
    const overlapOffset = 86.0;

    return Scaffold(
      backgroundColor: AppColors.background,
      body: SingleChildScrollView(
        child: Stack(
          children: [
            SizedBox(
              height: headerHeight,
              child: Stack(
                fit: StackFit.expand,
                children: [
                  _StationImage(path: detail.headerImagePath),
                  DecoratedBox(
                    decoration: BoxDecoration(
                      gradient: LinearGradient(
                        begin: Alignment.topCenter,
                        end: Alignment.bottomCenter,
                        colors: [
                          Colors.black.withValues(alpha: 0.06),
                          Colors.black.withValues(alpha: 0.18),
                          Colors.black.withValues(alpha: 0.58),
                        ],
                      ),
                    ),
                  ),
                  SafeArea(
                    bottom: false,
                    child: Padding(
                      padding: const EdgeInsets.fromLTRB(18, 10, 18, 22),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Row(
                            children: [
                              _CircularIconButton(
                                icon: Icons.arrow_back_ios_new_rounded,
                                onTap: () => Navigator.of(context).maybePop(),
                              ),
                              const SizedBox(width: 14),
                              const Spacer(),
                              _CircularIconButton(
                                icon: Icons.share_outlined,
                                onTap: () {},
                                foregroundColor: AppColors.background,
                                backgroundColor: Colors.transparent,
                              ),
                            ],
                          ),
                          const Spacer(),
                          Container(
                            padding: const EdgeInsets.symmetric(
                              horizontal: 18,
                              vertical: 8,
                            ),
                            decoration: BoxDecoration(
                              color: AppColors.brandBlue,
                              borderRadius: BorderRadius.circular(999),
                            ),
                            child: Text(
                              '${detail.line} • ${detail.hubLabel}',
                              style: const TextStyle(
                                color: AppColors.background,
                                fontWeight: FontWeight.w800,
                                fontSize: 13,
                              ),
                            ),
                          ),
                          const SizedBox(height: 14),
                          Text(
                            detail.name,
                            style: const TextStyle(
                              fontSize: 36,
                              height: 1,
                              fontWeight: FontWeight.w900,
                              color: AppColors.background,
                              shadows: [
                                Shadow(
                                  color: Color(0x55000000),
                                  blurRadius: 8,
                                  offset: Offset(0, 2),
                                ),
                              ],
                            ),
                          ),
                          const SizedBox(height: 10),
                          Row(
                            children: [
                              const Icon(
                                Icons.location_on_outlined,
                                size: 20,
                                color: AppColors.background,
                              ),
                              const SizedBox(width: 6),
                              Expanded(
                                child: Text(
                                  detail.location,
                                  style: const TextStyle(
                                    fontSize: 17,
                                    fontWeight: FontWeight.w600,
                                    color: AppColors.background,
                                  ),
                                ),
                              ),
                            ],
                          ),
                          const SizedBox(height: overlapOffset),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
            Container(
              margin: const EdgeInsets.only(top: headerHeight - 64),
              decoration: const BoxDecoration(
                color: AppColors.background,
                borderRadius: BorderRadius.vertical(top: Radius.circular(34)),
              ),
              child: Padding(
                padding: const EdgeInsets.fromLTRB(18, 96, 18, 28),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      children: [
                        Expanded(
                          child: _DetailActionCard(
                            icon: Icons.near_me_outlined,
                            label: 'Directions',
                            iconColor: AppColors.brandBlue,
                            onTap: onOpenDirections,
                          ),
                        ),
                        const SizedBox(width: 14),
                        Expanded(
                          child: _DetailActionCard(
                            icon: Icons.favorite_border_rounded,
                            label: 'Favorite',
                            iconColor: AppColors.brandRed,
                            onTap: () {},
                          ),
                        ),
                        const SizedBox(width: 14),
                        Expanded(
                          child: _DetailActionCard(
                            icon: Icons.explore_outlined,
                            label: 'Virtual Tour',
                            iconColor: AppColors.brandBlue,
                            onTap: () {},
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 34),
                    const _SectionTitle(title: 'Station History'),
                    const SizedBox(height: 14),
                    Container(
                      width: double.infinity,
                      padding: const EdgeInsets.fromLTRB(18, 18, 18, 16),
                      decoration: BoxDecoration(
                        color: const Color(0xFFF7F8FB),
                        borderRadius: BorderRadius.circular(24),
                      ),
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            detail.history,
                            style: const TextStyle(
                              fontSize: 15,
                              height: 1.7,
                              color: AppColors.textPrimary,
                            ),
                          ),
                          const SizedBox(height: 18),
                          const Divider(height: 1, color: Color(0xFFE3E8F0)),
                          const SizedBox(height: 16),
                          Row(
                            children: [
                              Expanded(
                                child: _HistoryMetaItem(
                                  icon: Icons.access_time_rounded,
                                  label: detail.openingHours,
                                ),
                              ),
                              const SizedBox(width: 16),
                              Expanded(
                                child: _HistoryMetaItem(
                                  icon: Icons.info_outline_rounded,
                                  label: detail.accessibility,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ),
                    const SizedBox(height: 34),
                    Row(
                      children: [
                        const Expanded(
                          child: _SectionTitle(title: 'Nearby Places'),
                        ),
                        TextButton.icon(
                          onPressed: () {},
                          iconAlignment: IconAlignment.end,
                          style: TextButton.styleFrom(
                            padding: EdgeInsets.zero,
                            foregroundColor: AppColors.brandBlue,
                          ),
                          icon: const Icon(
                            Icons.chevron_right_rounded,
                            size: 14,
                          ),
                          label: const Text(
                            'View All',
                            style: TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w800,
                            ),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 14),
                    SizedBox(
                      height: 234,
                      child: ListView.separated(
                        scrollDirection: Axis.horizontal,
                        itemCount: detail.places.length,
                        separatorBuilder: (_, __) => const SizedBox(width: 16),
                        itemBuilder: (context, index) {
                          final place = detail.places[index];
                          return _NearbyPlaceCard(place: place);
                        },
                      ),
                    ),
                    const SizedBox(height: 30),
                    SizedBox(
                      width: double.infinity,
                      child: ElevatedButton.icon(
                        onPressed: () {},
                        style: ElevatedButton.styleFrom(
                          backgroundColor: AppColors.brandRed,
                          foregroundColor: AppColors.background,
                          padding: const EdgeInsets.symmetric(vertical: 18),
                          shape: RoundedRectangleBorder(
                            borderRadius: BorderRadius.circular(18),
                          ),
                          textStyle: const TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w900,
                          ),
                        ),
                        icon: const Icon(Icons.auto_awesome_rounded),
                        label: const Text('COLLECT STATION STAMP'),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            Positioned(
              top: headerHeight - overlapOffset,
              left: 18,
              right: 18,
              child: _CollectorBanner(detail: detail),
            ),
          ],
        ),
      ),
    );
  }
}

class _CircularIconButton extends StatelessWidget {
  const _CircularIconButton({
    required this.icon,
    required this.onTap,
    this.foregroundColor = AppColors.textPrimary,
    this.backgroundColor = const Color(0xEBFFFFFF),
  });

  final IconData icon;
  final VoidCallback onTap;
  final Color foregroundColor;
  final Color backgroundColor;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: backgroundColor,
      shape: const CircleBorder(),
      child: InkWell(
        onTap: onTap,
        customBorder: const CircleBorder(),
        child: SizedBox(
          width: 42,
          height: 42,
          child: Icon(icon, color: foregroundColor, size: 20),
        ),
      ),
    );
  }
}

class _DetailActionCard extends StatelessWidget {
  const _DetailActionCard({
    required this.icon,
    required this.label,
    required this.iconColor,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final Color iconColor;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return Material(
      color: Colors.transparent,
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(24),
        child: Ink(
          height: 62,
          padding: const EdgeInsets.symmetric(horizontal: 1, vertical: 1),
          decoration: BoxDecoration(
            color: AppColors.background,
            borderRadius: BorderRadius.circular(24),
            border: Border.all(color: const Color(0xFFD8DDE6), width: 1.5),
          ),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              Icon(icon, color: iconColor, size: 20),
              const SizedBox(height: 6),
              Text(
                label,
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 14,
                  height: 1.15,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                ),
                maxLines: 2,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _SectionTitle extends StatelessWidget {
  const _SectionTitle({required this.title});

  final String title;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 4,
          height: 24,
          decoration: BoxDecoration(
            color: AppColors.brandBlue,
            borderRadius: BorderRadius.circular(999),
          ),
        ),
        const SizedBox(width: 8),
        Text(
          title,
          style: const TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.w900,
            color: AppColors.textPrimary,
          ),
        ),
      ],
    );
  }
}

class _CollectorBanner extends StatelessWidget {
  const _CollectorBanner({required this.detail});

  final _StationDetailData detail;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 14),
      decoration: BoxDecoration(
        color: const Color(0xFFF2F8FF),
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: const Color(0xFFD5E9FF), width: 1.5),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 16,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        children: [
          SizedBox(
            width: 110,
            height: 32,
            child: Stack(
              clipBehavior: Clip.none,
              children: List.generate(detail.collectorAvatars.length, (index) {
                final avatar = detail.collectorAvatars[index];
                final isCounter = index == detail.collectorAvatars.length - 1;
                return Positioned(
                  left: index * 22,
                  child: Container(
                    width: 32,
                    height: 32,
                    decoration: BoxDecoration(
                      shape: BoxShape.circle,
                      color: isCounter
                          ? AppColors.brandBlue
                          : const Color(0xFFFFF3F3),
                      border: Border.all(
                        color: AppColors.background,
                        width: 2.5,
                      ),
                      image: isCounter
                          ? null
                          : DecorationImage(
                              image: NetworkImage(
                                'https://i.pravatar.cc/120?img=${index + 12}',
                              ),
                              fit: BoxFit.cover,
                            ),
                    ),
                    child: isCounter
                        ? Center(
                            child: Text(
                              avatar,
                              style: const TextStyle(
                                fontSize: 12,
                                fontWeight: FontWeight.w900,
                                color: AppColors.background,
                              ),
                            ),
                          )
                        : null,
                  ),
                );
              }),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Text(
              detail.collectorText,
              style: const TextStyle(
                fontSize: 14,
                height: 1.25,
                fontWeight: FontWeight.w900,
                color: Color(0xFF0B3C69),
              ),
            ),
          ),
          const SizedBox(width: 12),
          const Icon(
            Icons.auto_awesome_outlined,
            size: 24,
            color: Color(0xFF5B9BD5),
          ),
        ],
      ),
    );
  }
}

class _HistoryMetaItem extends StatelessWidget {
  const _HistoryMetaItem({required this.icon, required this.label});

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Icon(icon, size: 20, color: AppColors.brandBlue),
        const SizedBox(width: 5),
        Expanded(
          child: Text(
            label,
            style: const TextStyle(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: AppColors.textPrimary,
            ),
          ),
        ),
      ],
    );
  }
}

class _NearbyPlaceCard extends StatelessWidget {
  const _NearbyPlaceCard({required this.place});

  final _NearbyPlace place;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 188,
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.background,
          borderRadius: BorderRadius.circular(22),
          boxShadow: const [
            BoxShadow(
              color: AppColors.shadow,
              blurRadius: 14,
              offset: Offset(0, 8),
            ),
          ],
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Expanded(
              child: ClipRRect(
                borderRadius:
                    const BorderRadius.vertical(top: Radius.circular(22)),
                child: Stack(
                  fit: StackFit.expand,
                  children: [
                    _StationImage(path: place.imagePath),
                    Positioned(
                      top: 8,
                      right: 8,
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                          horizontal: 8,
                          vertical: 4,
                        ),
                        decoration: BoxDecoration(
                          color: AppColors.background.withValues(alpha: 0.9),
                          borderRadius: BorderRadius.circular(999),
                        ),
                        child: Text(
                          place.distance,
                          style: const TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w800,
                          ),
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            Padding(
              padding: const EdgeInsets.fromLTRB(16, 12, 16, 16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    place.tag,
                    style: const TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w800,
                      color: AppColors.brandBlue,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    place.name,
                    style: const TextStyle(
                      fontSize: 17,
                      fontWeight: FontWeight.w800,
                      color: AppColors.textPrimary,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _StationImage extends StatelessWidget {
  const _StationImage({required this.path});

  final String path;

  @override
  Widget build(BuildContext context) {
    if (path.startsWith('http://') || path.startsWith('https://')) {
      return Image.network(
        path,
        fit: BoxFit.cover,
        errorBuilder: (context, error, stackTrace) {
          return _StationImageFallback();
        },
        loadingBuilder: (context, child, loadingProgress) {
          if (loadingProgress == null) {
            return child;
          }

          return const DecoratedBox(
            decoration: BoxDecoration(color: Color(0xFFF2F5F9)),
            child: Center(
              child: SizedBox(
                width: 24,
                height: 24,
                child: CircularProgressIndicator(strokeWidth: 2.4),
              ),
            ),
          );
        },
      );
    }

    return Image.asset(
      path,
      fit: BoxFit.cover,
      errorBuilder: (context, error, stackTrace) {
        return _StationImageFallback();
      },
    );
  }
}

class _StationImageFallback extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return DecoratedBox(
      decoration: const BoxDecoration(
        gradient: LinearGradient(
          colors: [Color(0xFFE9EEF5), Color(0xFFDCE5EF)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
      ),
      child: Center(
        child: Icon(
          Icons.image_outlined,
          size: 34,
          color: AppColors.textMuted.withValues(alpha: 0.75),
        ),
      ),
    );
  }
}

class _StationsBottomBar extends StatelessWidget {
  const _StationsBottomBar({
    required this.selectedIndex,
    required this.onSelected,
  });

  final int selectedIndex;
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    return BottomAppBar(
      height: 78,
      padding: const EdgeInsets.symmetric(horizontal: 8),
      color: AppColors.background,
      surfaceTintColor: AppColors.background,
      child: Row(
        children: [
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _StationNavItem(
                  icon: Icons.home_outlined,
                  label: 'Home',
                  isActive: selectedIndex == 0,
                  onTap: () => onSelected(0),
                ),
                _StationNavItem(
                  icon: Icons.book_outlined,
                  label: 'Book',
                  isActive: selectedIndex == 1,
                  onTap: () => onSelected(1),
                ),
              ],
            ),
          ),
          const SizedBox(width: 56),
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _StationNavItem(
                  icon: Icons.format_list_bulleted_rounded,
                  label: 'Stations',
                  isActive: selectedIndex == 2,
                  onTap: () => onSelected(2),
                ),
                _StationNavItem(
                  icon: Icons.person_outline_rounded,
                  label: 'Profile',
                  isActive: selectedIndex == 3,
                  onTap: () => onSelected(3),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _StationNavItem extends StatelessWidget {
  const _StationNavItem({
    required this.icon,
    required this.label,
    required this.isActive,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 6),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              color: isActive ? AppColors.brandBlue : AppColors.textPrimary,
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w700,
                color: isActive ? AppColors.brandBlue : AppColors.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _StationDetailData {
  const _StationDetailData({
    required this.name,
    required this.line,
    required this.location,
    required this.headerImagePath,
    required this.hubLabel,
    required this.collectorText,
    required this.collectorAvatars,
    required this.history,
    required this.openingHours,
    required this.accessibility,
    required this.places,
  });

  final String name;
  final String line;
  final String location;
  final String headerImagePath;
  final String hubLabel;
  final String collectorText;
  final List<String> collectorAvatars;
  final String history;
  final String openingHours;
  final String accessibility;
  final List<_NearbyPlace> places;

  static _StationDetailData fromStation(StationItem station) {
    final normalizedName = station.name.toLowerCase();

    if (normalizedName.contains('bến thành')) {
      return _StationDetailData(
        name: station.name,
        line: station.line,
        location: 'District 1, Ho Chi Minh City',
        headerImagePath:
            'https://lh3.googleusercontent.com/gps-cs-s/AHVAwepmPwsnxaVx24D5O_CPxp86xCk9zl9oQohp3YECX7ny4h0QqW2ElaHa3EWaJHKAQ0cwM5Ae4SAwMaslToDoITGdE7g0qFK6sHKMJ6QU6GrxHlLs64l67UdaVBdzTkxi7G_hfOV3vrFs5pHT=w408-h306-k-no',
        hubLabel: 'Central Hub',
        collectorText: '1,248 collectors stamped here this week',
        collectorAvatars: const ['A', 'L', 'DK', '9k'],
        history:
            'As the grand central station of the HCMC Metro system, Bến Thành serves as the primary interchange for multiple lines and sits beside the city\'s most iconic market district. The station blends contemporary transit design with references to Sai Gon\'s trading heritage.',
        openingHours: 'Opens 05:00 AM',
        accessibility: '4 Accessible Levels',
        places: const [
          _NearbyPlace(
            name: 'Bến Thành Market',
            tag: 'LANDMARK',
            distance: '120m',
            imagePath:
                'https://images.unsplash.com/photo-1555992336-03a23c7b20ee?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'September 23rd Park',
            tag: 'PARK',
            distance: '350m',
            imagePath:
                'https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'Takashimaya',
            tag: 'SHOPPING',
            distance: '500m',
            imagePath:
                'https://images.unsplash.com/photo-1481437156560-3205f6a55735?auto=format&fit=crop&w=800&q=80',
          ),
        ],
      );
    }

    if (normalizedName.contains('nhà hát')) {
      return _StationDetailData(
        name: station.name,
        line: station.line,
        location: 'Dong Khoi, District 1',
        headerImagePath:
            'https://lh3.googleusercontent.com/gps-cs-s/AHVAwep4WFVd7xhheLVSF77yx7Oe-cjho10vYh19UYjJ5z_12KsGiwXsmSWm8Rh9CO1ElNNgHgc-MMXqyfNyPCiefQO5lrjFhXFSc1NF1I0s46zHkWRScLjXPWNOy_ApErCcYVLYCKoHFveGgcOE=w408-h306-k-no',
        hubLabel: 'Culture Stop',
        collectorText: '864 collectors checked in here this week',
        collectorAvatars: const ['MH', 'Q', 'T', '6k'],
        history:
            'Stationed near the Municipal Theatre, this stop is shaped by the city\'s French colonial core and today acts as a gateway to Dong Khoi, Nguyen Hue, and the surrounding arts corridor. The design emphasizes quick pedestrian flow between entertainment venues and office towers.',
        openingHours: 'Opens 05:00 AM',
        accessibility: '3 Accessible Levels',
        places: const [
          _NearbyPlace(
            name: 'Saigon Opera House',
            tag: 'CULTURE',
            distance: '90m',
            imagePath:
                'https://images.unsplash.com/photo-1520637836862-4d197d17c35a?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'Nguyen Hue Walking Street',
            tag: 'CITY WALK',
            distance: '250m',
            imagePath:
                'https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'Union Square',
            tag: 'SHOPPING',
            distance: '300m',
            imagePath:
                'https://images.unsplash.com/photo-1519567241046-7f570eee3ce6?auto=format&fit=crop&w=800&q=80',
          ),
        ],
      );
    }

    if (normalizedName.contains('tân cảng')) {
      return _StationDetailData(
        name: station.name,
        line: station.line,
        location: 'Dong Khoi, District 1',
        headerImagePath:
            'https://lh3.googleusercontent.com/gps-cs-s/AHVAweqH-FTeVOtPB6ffo5VFBp0IpRAI4g5LuKtLNNK6D8EA90j9GizC_O68L1sWwnK7t3qAJ99YigUpLRRpAxsRLiUsklrrXRNqD0Vn9DaUeaDZ4I-x78G4BKV-gfIp-g_PbWlm5i6G=w408-h306-k-no',
        hubLabel: 'Culture Stop',
        collectorText: '864 collectors checked in here this week',
        collectorAvatars: const ['MH', 'Q', 'T', '6k'],
        history:
            'Stationed near the Municipal Theatre, this stop is shaped by the city\'s French colonial core and today acts as a gateway to Dong Khoi, Nguyen Hue, and the surrounding arts corridor. The design emphasizes quick pedestrian flow between entertainment venues and office towers.',
        openingHours: 'Opens 05:00 AM',
        accessibility: '3 Accessible Levels',
        places: const [
          _NearbyPlace(
            name: 'Saigon Opera House',
            tag: 'CULTURE',
            distance: '90m',
            imagePath:
                'https://images.unsplash.com/photo-1520637836862-4d197d17c35a?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'Nguyen Hue Walking Street',
            tag: 'CITY WALK',
            distance: '250m',
            imagePath:
                'https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'Union Square',
            tag: 'SHOPPING',
            distance: '300m',
            imagePath:
                'https://images.unsplash.com/photo-1519567241046-7f570eee3ce6?auto=format&fit=crop&w=800&q=80',
          ),
        ],
      );
    }

    if (normalizedName.contains('an phú')) {
      return _StationDetailData(
        name: station.name,
        line: station.line,
        location: 'Dong Khoi, District 1',
        headerImagePath:
            'https://lh3.googleusercontent.com/gps-cs-s/AHVAwerKWFWR9qh4-I1s2btDLMQTWMzWnTZvSsx73660qxrBdKXzhN71Bc3uI8Eh87mPvM1__92PyXgn3drzqN69NZA1SmcbJ2ZFrv0UeV5f-Tq4haXsaRbzxcH2KzpikZTOnmaGbn6DhZNvN3fQ=w408-h306-k-no',
        hubLabel: 'Culture Stop',
        collectorText: '864 collectors checked in here this week',
        collectorAvatars: const ['MH', 'Q', 'T', '6k'],
        history:
            'Stationed near the Municipal Theatre, this stop is shaped by the city\'s French colonial core and today acts as a gateway to Dong Khoi, Nguyen Hue, and the surrounding arts corridor. The design emphasizes quick pedestrian flow between entertainment venues and office towers.',
        openingHours: 'Opens 05:00 AM',
        accessibility: '3 Accessible Levels',
        places: const [
          _NearbyPlace(
            name: 'Saigon Opera House',
            tag: 'CULTURE',
            distance: '90m',
            imagePath:
                'https://images.unsplash.com/photo-1520637836862-4d197d17c35a?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'Nguyen Hue Walking Street',
            tag: 'CITY WALK',
            distance: '250m',
            imagePath:
                'https://images.unsplash.com/photo-1499856871958-5b9627545d1a?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'Union Square',
            tag: 'SHOPPING',
            distance: '300m',
            imagePath:
                'https://images.unsplash.com/photo-1519567241046-7f570eee3ce6?auto=format&fit=crop&w=800&q=80',
          ),
        ],
      );
    }

    if (normalizedName.contains('ba son')) {
      return _StationDetailData(
        name: station.name,
        line: station.line,
        location: 'Thu Duc Riverside Corridor',
        headerImagePath:
            'https://lh3.googleusercontent.com/gps-cs-s/AHVAwepKKL08T5WPb2AHbUTKP3cgbiNBlcx-AfedzQukDtzUXp_WAiLfxdZ46IfX7aDMuVtlzvX4y42lyCyy2fKrgsuRT87ihnP4yrYjJXwXqCjesTv5KcnM9QxHLigJ9MotByayYOQ=w1200-h900-k-no',
        hubLabel: 'Riverfront Access',
        collectorText: '1,024 collectors stamped here this week',
        collectorAvatars: const ['HN', 'P', 'AK', '7k'],
        history:
            'Ba Son Station rises from the former shipyard zone beside the Sai Gon River and anchors one of the city\'s fastest-changing waterfront districts. Its location links metro riders to riverside promenades, mixed-use towers, and the historic memory of the Ba Son workshop area.',
        openingHours: 'Opens 05:00 AM',
        accessibility: '4 Accessible Levels',
        places: const [
          _NearbyPlace(
            name: 'Saigon River Park',
            tag: 'RIVERSIDE',
            distance: '180m',
            imagePath:
                'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'Landmark 81',
            tag: 'SKYLINE',
            distance: '900m',
            imagePath:
                'https://images.unsplash.com/photo-1477959858617-67f85cf4f1df?auto=format&fit=crop&w=800&q=80',
          ),
          _NearbyPlace(
            name: 'Bach Dang Wharf',
            tag: 'LANDMARK',
            distance: '1.1km',
            imagePath:
                'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80',
          ),
        ],
      );
    }

    return _StationDetailData(
      name: station.name,
      line: station.line,
      location: 'Ho Chi Minh City Metro Corridor',
      headerImagePath: station.imagePath,
      hubLabel: 'Local Stop',
      collectorText: 'Collectors are starting to discover this station',
      collectorAvatars: const ['M', 'E', 'T', '2k'],
      history:
          '${station.name} is one of the metro stops helping connect residential districts, schools, and commercial clusters across the city. This page can be expanded with station-specific history, amenities, and local highlights as your content library grows.',
      openingHours: 'Opens 05:00 AM',
      accessibility: '2 Accessible Levels',
      places: const [
        _NearbyPlace(
          name: 'Community Plaza',
          tag: 'PUBLIC SPACE',
          distance: '200m',
          imagePath:
              'https://images.unsplash.com/photo-1494526585095-c41746248156?auto=format&fit=crop&w=800&q=80',
        ),
        _NearbyPlace(
          name: 'Local Cafe Street',
          tag: 'FOOD',
          distance: '420m',
          imagePath:
              'https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?auto=format&fit=crop&w=800&q=80',
        ),
        _NearbyPlace(
          name: 'Neighborhood Park',
          tag: 'PARK',
          distance: '700m',
          imagePath:
              'https://images.unsplash.com/photo-1441974231531-c6227db76b6e?auto=format&fit=crop&w=800&q=80',
        ),
      ],
    );
  }
}

class _NearbyPlace {
  const _NearbyPlace({
    required this.name,
    required this.tag,
    required this.distance,
    required this.imagePath,
  });

  final String name;
  final String tag;
  final String distance;
  final String imagePath;
}

import { createBrowserRouter, Navigate } from 'react-router-dom';
import { ProtectedRoute } from './ProtectedRoute';
import { AppLayout } from '@/components/layout/AppLayout';
import { LoginPage } from '@/pages/LoginPage';
import { DashboardPage } from '@/pages/DashboardPage';
import { ForbiddenPage } from '@/pages/ForbiddenPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { MyProfilePage } from '@/pages/MyProfilePage';
import { RequestsListPage } from '@/pages/requests/RequestsListPage';
import { CreateRequestPage } from '@/pages/requests/CreateRequestPage';
import { RequestDetailsPage } from '@/pages/requests/RequestDetailsPage';
import { MyRequestsPage } from '@/pages/requests/MyRequestsPage';
import { ExecutorPoolPage } from '@/pages/executor/ExecutorPoolPage';
import { ResidentsListPage } from '@/pages/admin/ResidentsListPage';
import { ResidentDetailsPage } from '@/pages/admin/ResidentDetailsPage';
import { EnrollResidentPage } from '@/pages/admin/EnrollResidentPage';
import { RoomsListPage } from '@/pages/admin/RoomsListPage';
import { RoomDetailsPage } from '@/pages/admin/RoomDetailsPage';
import { UsersListPage } from '@/pages/admin/UsersListPage';
import { AppliancesModerationPage } from '@/pages/admin/AppliancesModerationPage';
import { MyAppliancesPage } from '@/pages/resident/MyAppliancesPage';
import { ConsumableTypesPage } from '@/pages/property-manager/ConsumableTypesPage';
import { ConsumableStockPage } from '@/pages/property-manager/ConsumableStockPage';
import { IssuanceJournalPage } from '@/pages/property-manager/IssuanceJournalPage';
import { MyConsumablesPage } from '@/pages/resident/MyConsumablesPage';
import { NotificationsPage } from '@/pages/notifications/NotificationsPage';
import { NotificationSettingsPage } from '@/pages/notifications/NotificationSettingsPage';
import { ReportsPage } from '@/pages/admin/reports/ReportsPage';
import { RequestsReportPage } from '@/pages/admin/reports/RequestsReportPage';
import { AssigneesReportPage } from '@/pages/admin/reports/AssigneesReportPage';
import { ResidentsReportPage } from '@/pages/admin/reports/ResidentsReportPage';
import { AppliancesReportPage } from '@/pages/admin/reports/AppliancesReportPage';
import { ConsumablesReportPage } from '@/pages/admin/reports/ConsumablesReportPage';
import { RoleRoute } from './RoleRoute';
import { ROLES } from '@/shared/constants/roles';

export const router = createBrowserRouter([
  {
    path: '/login',
    element: <LoginPage />,
  },
  {
    path: '/forbidden',
    element: <ForbiddenPage />,
  },
  {
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/dashboard" replace /> },
      { path: 'dashboard', element: <DashboardPage /> },
      { path: 'profile', element: <MyProfilePage /> },
      { path: 'requests', element: <RequestsListPage /> },
      {
        path: 'requests/new',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN, ROLES.RESIDENT]}>
            <CreateRequestPage />
          </RoleRoute>
        ),
      },
      {
        path: 'requests/pool',
        element: (
          <RoleRoute
            allowedRoles={[
              ROLES.ADMIN,
              ROLES.PROPERTY_MANAGER,
              ROLES.EXECUTOR_ELECTRIC,
              ROLES.EXECUTOR_PLUMBING,
              ROLES.EXECUTOR_CARPENTRY,
              ROLES.EXECUTOR_GAS,
            ]}
          >
            <ExecutorPoolPage />
          </RoleRoute>
        ),
      },
      { path: 'my/requests', element: <MyRequestsPage /> },
      { path: 'requests/:id', element: <RequestDetailsPage /> },
      {
        path: 'residents',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <ResidentsListPage />
          </RoleRoute>
        ),
      },
      {
        path: 'residents/new',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <EnrollResidentPage />
          </RoleRoute>
        ),
      },
      {
        path: 'residents/:id',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <ResidentDetailsPage />
          </RoleRoute>
        ),
      },
      {
        path: 'rooms',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <RoomsListPage />
          </RoleRoute>
        ),
      },
      {
        path: 'rooms/:id',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <RoomDetailsPage />
          </RoleRoute>
        ),
      },
      {
        path: 'users',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <UsersListPage />
          </RoleRoute>
        ),
      },
      {
        path: 'appliances',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <AppliancesModerationPage />
          </RoleRoute>
        ),
      },
      {
        path: 'my/appliances',
        element: (
          <RoleRoute allowedRoles={[ROLES.RESIDENT]}>
            <MyAppliancesPage />
          </RoleRoute>
        ),
      },
      {
        path: 'consumables/types',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN, ROLES.PROPERTY_MANAGER]}>
            <ConsumableTypesPage />
          </RoleRoute>
        ),
      },
      {
        path: 'consumables/stock',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN, ROLES.PROPERTY_MANAGER]}>
            <ConsumableStockPage />
          </RoleRoute>
        ),
      },
      {
        path: 'consumables/issues',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN, ROLES.PROPERTY_MANAGER]}>
            <IssuanceJournalPage />
          </RoleRoute>
        ),
      },
      {
        path: 'my/consumables',
        element: (
          <RoleRoute allowedRoles={[ROLES.RESIDENT]}>
            <MyConsumablesPage />
          </RoleRoute>
        ),
      },
      { path: 'notifications', element: <NotificationsPage /> },
      { path: 'notifications/settings', element: <NotificationSettingsPage /> },
      {
        path: 'reports',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <ReportsPage />
          </RoleRoute>
        ),
      },
      {
        path: 'reports/requests',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <RequestsReportPage />
          </RoleRoute>
        ),
      },
      {
        path: 'reports/assignees',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <AssigneesReportPage />
          </RoleRoute>
        ),
      },
      {
        path: 'reports/residents',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <ResidentsReportPage />
          </RoleRoute>
        ),
      },
      {
        path: 'reports/appliances',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <AppliancesReportPage />
          </RoleRoute>
        ),
      },
      {
        path: 'reports/consumables',
        element: (
          <RoleRoute allowedRoles={[ROLES.ADMIN]}>
            <ConsumablesReportPage />
          </RoleRoute>
        ),
      },
      { path: '*', element: <NotFoundPage /> },
    ],
  },
]);

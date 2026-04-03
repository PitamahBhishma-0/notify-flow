export interface NotificationRequest {
  recipientUserId: string;
  subject: string;
  body: string;
  channel: 'EMAIL' | 'SMS' | 'PUSH';
  priority: 'HIGH' | 'MEDIUM' | 'LOW';
}

export interface NotificationResponse {
  id: string;
  status: string;
  timestamp: string;
}
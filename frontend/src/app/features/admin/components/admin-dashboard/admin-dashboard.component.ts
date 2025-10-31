import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AdminStats, SystemLog } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  totalUsers = 0;
  activeSubscriptions = 0;
  monthlyRevenue = 0;
  newRegistrations = 0; // Non fourni côté API pour l'instant

  recentActivities: { time: string; description: string }[] = [];

  constructor(private adminService: AdminService) {}

  ngOnInit(): void {
    this.loadStats();
    this.loadRecentLogs();
  }

  private loadStats(): void {
    this.adminService.getAdminStats().subscribe({
      next: (stats: AdminStats) => {
        this.totalUsers = Number(stats.totalUsers || 0);
        const basic = Number(stats.basicUsers || 0);
        const premium = Number(stats.premiumUsers || 0);
        this.activeSubscriptions = basic + premium;
        this.monthlyRevenue = Number(stats.monthlyRevenue || 0);
      },
      error: (err) => {
        console.error('Erreur chargement stats admin', err);
      }
    });
  }

  private loadRecentLogs(): void {
    this.adminService.getSystemLogs().subscribe({
      next: (logs: SystemLog[]) => {
        const top = (logs || []).slice(0, 5);
        this.recentActivities = top.map(l => ({
          time: new Date(l.timestamp).toLocaleString(),
          description: l.message || l.type
        }));
      },
      error: (err) => {
        console.error('Erreur chargement logs admin', err);
      }
    });
  }
}

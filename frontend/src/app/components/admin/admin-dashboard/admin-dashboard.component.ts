import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, AdminStats } from '../../../core/services/admin.service';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.css']
})
export class AdminDashboardComponent implements OnInit {
  stats: AdminStats = {
    totalUsers: 1247,
    totalSearches: 5892,
    totalProfiles: 127000,
    monthlyRevenue: 42500,
    freeUsers: 847,
    basicUsers: 312,
    premiumUsers: 88
  };

  loading = false;
  error: string | null = null;

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadStats();
  }

  loadStats() {
    this.loading = true;
    this.adminService.getAdminStats().subscribe({
      next: (data) => {
        this.stats = data;
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des statistiques';
        this.loading = false;
        console.error(err);
        // Fallback to mock data on error
        this.stats = {
          totalUsers: 1247,
          totalSearches: 5892,
          totalProfiles: 127000,
          monthlyRevenue: 42500,
          freeUsers: 847,
          basicUsers: 312,
          premiumUsers: 88
        };
      }
    });
  }
}

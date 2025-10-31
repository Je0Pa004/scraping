import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, ScrapingActivity } from '../../../core/services/admin.service';
import { NotificationService } from '../../../core/services/notification.service';

@Component({
  selector: 'app-scraping-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './scraping-management.component.html',
  styleUrl: './scraping-management.component.css'
})
export class ScrapingManagementComponent implements OnInit {
  scrapingActivities: ScrapingActivity[] = [];
  filteredActivities: ScrapingActivity[] = [];
  groupedActivities: { user: string; email: string; activities: ScrapingActivity[] }[] = [];
  loading = false;
  error: string | null = null;
  
  // Filters
  searchTerm = '';
  selectedStatus = 'all';
  selectedSource = 'all';
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;
  private expandedGroups = new Set<string>();
  
  // Stats
  get totalSearches() {
    return this.scrapingActivities.length;
  }
  
  get totalProfiles() {
    return this.scrapingActivities.reduce((sum, a) => sum + a.profilesFound, 0);
  }
  
  get activeSearches() {
    return this.scrapingActivities.filter(a => a.status === 'running').length;
  }

  get failedCount() {
    return this.scrapingActivities.filter(a => a.status === 'failed').length;
  }

  get displayedFrom() {
    return this.groupedActivities.length ? (this.currentPage - 1) * this.itemsPerPage + 1 : 0;
  }

  get displayedTo() {
    const to = this.currentPage * this.itemsPerPage;
    return Math.min(to, this.groupedActivities.length);
  }

  constructor(private adminService: AdminService, private notification: NotificationService) {}

  ngOnInit() {
    this.loadScrapingActivities();
  }

  loadScrapingActivities() {
    this.loading = true;
    this.adminService.getAllScrapingActivities().subscribe({
      next: (data) => {
        this.scrapingActivities = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des activités de scraping';
        this.loading = false;
        console.error(err);
        this.notification.showError('Erreur lors du chargement des activités de scraping');
      }
    });
  }

  applyFilters() {
    this.filteredActivities = this.scrapingActivities.filter(activity => {
      const matchesSearch = activity.user.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           activity.email.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           activity.criteria.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesStatus = this.selectedStatus === 'all' || activity.status === this.selectedStatus;
      const matchesSource = this.selectedSource === 'all' || activity.source === this.selectedSource;
      
      return matchesSearch && matchesStatus && matchesSource;
    });

    // Group by email (fallback to user when email missing)
    const map = new Map<string, { user: string; email: string; activities: ScrapingActivity[] }>();
    for (const a of this.filteredActivities) {
      const key = a.email || a.user;
      if (!map.has(key)) {
        map.set(key, { user: a.user, email: a.email, activities: [] });
      }
      map.get(key)!.activities.push(a);
    }
    this.groupedActivities = Array.from(map.values());

    this.totalPages = Math.ceil(this.groupedActivities.length / this.itemsPerPage);
    this.currentPage = 1;
  }

  get paginatedGroups() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.groupedActivities.slice(start, end);
  }

  isExpanded(email: string) {
    return this.expandedGroups.has(email);
  }

  toggleGroup(email: string) {
    if (this.expandedGroups.has(email)) {
      this.expandedGroups.delete(email);
    } else {
      this.expandedGroups.add(email);
    }
  }

  stopScraping(scrapingId: string) {
    if (confirm('Êtes-vous sûr de vouloir arrêter cette recherche ?')) {
      this.adminService.stopScraping(scrapingId).subscribe({
        next: () => {
          this.loadScrapingActivities();
          this.notification.showSuccess('Scraping arrêté');
        },
        error: (err) => {
          this.notification.showError('Erreur lors de l\'arrêt de la recherche');
          console.error(err);
        }
      });
    }
  }

  nextPage() {
    if (this.currentPage < this.totalPages) {
      this.currentPage++;
    }
  }

  previousPage() {
    if (this.currentPage > 1) {
      this.currentPage--;
    }
  }
}

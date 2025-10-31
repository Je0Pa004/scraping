import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, SystemLog } from '../../../core/services/admin.service';

@Component({
  selector: 'app-logs-management',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './logs-management.component.html',
  styleUrl: './logs-management.component.css'
})
export class LogsManagementComponent implements OnInit {
  logs: SystemLog[] = [];
  filteredLogs: SystemLog[] = [];
  loading = false;
  error: string | null = null;
  
  // Filters
  searchTerm = '';
  selectedType = 'all';
  selectedSource = 'all';
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;
  
  // Stats
  get errorCount() {
    return this.logs.filter(l => l.type === 'error').length;
  }
  
  get warningCount() {
    return this.logs.filter(l => l.type === 'warning').length;
  }
  
  get infoCount() {
    return this.logs.filter(l => l.type === 'info').length;
  }

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadLogs();
  }

  loadLogs() {
    this.loading = true;
    this.adminService.getSystemLogs().subscribe({
      next: (data) => {
        this.logs = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des logs';
        this.loading = false;
        console.error(err);
      }
    });
  }

  applyFilters() {
    this.filteredLogs = this.logs.filter(log => {
      const matchesSearch = log.message.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           (log.user && log.user.toLowerCase().includes(this.searchTerm.toLowerCase()));
      const matchesType = this.selectedType === 'all' || log.type === this.selectedType;
      const matchesSource = this.selectedSource === 'all' || log.source === this.selectedSource;
      
      return matchesSearch && matchesType && matchesSource;
    });
    
    this.totalPages = Math.ceil(this.filteredLogs.length / this.itemsPerPage);
    this.currentPage = 1;
  }

  get paginatedLogs() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filteredLogs.slice(start, end);
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

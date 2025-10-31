import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin.service';

@Component({
  selector: 'app-users-management',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './users-management.component.html',
  styleUrl: './users-management.component.css'
})
export class UsersManagementComponent implements OnInit {
  users: any[] = [];
  filteredUsers: any[] = [];
  loading = false;
  error: string | null = null;
  showModal = false;
  modalMode: 'create' | 'edit' | 'view' = 'create';
  selectedUser: any = null;
  
  // Filters
  searchTerm = '';
  selectedPlan = 'all';
  selectedStatus = 'all';
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;

  constructor(private adminService: AdminService) {}

  ngOnInit() {
    this.loadUsers();
  }

  loadUsers() {
    this.loading = true;
    this.adminService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.applyFilters();
        this.loading = false;
      },
      error: (err) => {
        this.error = 'Erreur lors du chargement des utilisateurs';
        this.loading = false;
        console.error(err);
      }
    });
  }

  applyFilters() {
    this.filteredUsers = this.users.filter(user => {
      const matchesSearch = user.fullName.toLowerCase().includes(this.searchTerm.toLowerCase()) ||
                           user.email.toLowerCase().includes(this.searchTerm.toLowerCase());
      const matchesPlan = this.selectedPlan === 'all' || user.plan === this.selectedPlan;
      const matchesStatus = this.selectedStatus === 'all' || user.status === this.selectedStatus;
      
      return matchesSearch && matchesPlan && matchesStatus;
    });
    
    this.totalPages = Math.ceil(this.filteredUsers.length / this.itemsPerPage);
    this.currentPage = 1;
  }

  get paginatedUsers() {
    const start = (this.currentPage - 1) * this.itemsPerPage;
    const end = start + this.itemsPerPage;
    return this.filteredUsers.slice(start, end);
  }

  suspendUser(userId: string) {
    if (confirm('Êtes-vous sûr de vouloir suspendre cet utilisateur ?')) {
      this.adminService.suspendUser(userId).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (err) => {
          alert('Erreur lors de la suspension de l\'utilisateur');
          console.error(err);
        }
      });
    }
  }

  activateUser(userId: string) {
    this.adminService.activateUser(userId).subscribe({
      next: () => {
        this.loadUsers();
      },
      error: (err) => {
        alert('Erreur lors de l\'activation de l\'utilisateur');
        console.error(err);
      }
    });
  }

  deleteUser(userId: string) {
    if (confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ? Cette action est irréversible.')) {
      this.adminService.deleteUser(userId).subscribe({
        next: () => {
          this.loadUsers();
        },
        error: (err) => {
          alert('Erreur lors de la suppression de l\'utilisateur');
          console.error(err);
        }
      });
    }
  }

  viewUserDetails(userId: string) {
    this.modalMode = 'view';
    this.selectedUser = this.users.find(u => u.id === userId);
    this.showModal = true;
  }

  editUser(user: any) {
    this.modalMode = 'edit';
    this.selectedUser = { ...user };
    this.showModal = true;
  }

  openCreateModal() {
    this.modalMode = 'create';
    this.selectedUser = {
      fullName: '',
      email: '',
      plan: 'Free',
      status: 'ACTIVE'
    };
    this.showModal = true;
  }

  closeModal() {
    this.showModal = false;
    this.selectedUser = null;
  }

  saveUser() {
    if (this.modalMode === 'create') {
      // Pour la création, nous utilisons l'endpoint register existant
      // Vous pouvez adapter selon vos besoins
      alert('Fonctionnalité de création à implémenter avec l\'endpoint register');
    } else if (this.modalMode === 'edit') {
      this.adminService.updateUser(this.selectedUser.id, this.selectedUser).subscribe({
        next: () => {
          this.loadUsers();
          this.closeModal();
        },
        error: (err) => {
          alert('Erreur lors de la mise à jour');
          console.error(err);
        }
      });
    }
  }

  exportToCSV() {
    const csvContent = this.convertToCSV(this.filteredUsers);
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.setAttribute('href', url);
    link.setAttribute('download', 'users_export.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  }

  private convertToCSV(data: any[]): string {
    if (!data || data.length === 0) return '';
    
    const headers = ['ID', 'Nom', 'Email', 'Plan', 'Statut', 'Recherches', 'Quota %'];
    const rows = data.map(user => [
      user.id,
      user.fullName,
      user.email,
      user.plan,
      user.status,
      user.searchesUsed,
      user.quotaPercentage
    ]);
    
    return [
      headers.join(','),
      ...rows.map(row => row.join(','))
    ].join('\n');
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

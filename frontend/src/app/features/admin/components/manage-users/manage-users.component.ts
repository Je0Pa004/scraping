import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService, UserManagement } from '../../../../core/services/admin.service';

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './manage-users.component.html',
  styleUrl: './manage-users.component.css'
})
export class ManageUsersComponent implements OnInit {
  users: UserManagement[] = [];
  filteredUsers: UserManagement[] = [];
  loading = false;
  error: string | null = null;
  
  // Filters
  searchTerm = '';
  selectedStatus = 'all';
  selectedPlan = 'all';
  
  // Pagination
  currentPage = 1;
  itemsPerPage = 10;
  totalPages = 1;
  Math = Math;
  
  // Modal management
  showModal = false;
  showDeleteModal = false;
  isEditMode = false;
  userToDelete: UserManagement | null = null;
  currentUser: Partial<UserManagement> & { password?: string } = {
    fullName: '',
    email: '',
    roles: 'USER',
    status: 'ACTIVE',
    password: ''
  };
  
  // Stats
  get totalUsers() {
    return this.users.length;
  }
  
  get activeUsers() {
    return this.users.filter(u => u.status === 'ACTIVE').length;
  }
  
  get suspendedUsers() {
    return this.users.filter(u => u.status === 'SUSPENDED').length;
  }
  
  get premiumUsers() {
    return this.users.filter(u => u.plan === 'Premium').length;
  }
  
  get activePercentage() {
    return this.totalUsers > 0 ? Math.round((this.activeUsers / this.totalUsers) * 100) : 0;
  }
  
  get premiumPercentage() {
    return this.totalUsers > 0 ? Math.round((this.premiumUsers / this.totalUsers) * 100) : 0;
  }
  
  get newUsersThisMonth() {
    const now = new Date();
    const thisMonth = this.users.filter(u => {
      const regDate = new Date(u.registrationDate);
      return regDate.getMonth() === now.getMonth() && regDate.getFullYear() === now.getFullYear();
    });
    return thisMonth.length;
  }

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
      const matchesStatus = this.selectedStatus === 'all' || user.status === this.selectedStatus;
      const matchesPlan = this.selectedPlan === 'all' || user.plan === this.selectedPlan;
      
      return matchesSearch && matchesStatus && matchesPlan;
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
          alert('Erreur lors de la suspension');
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
        alert('Erreur lors de l\'activation');
        console.error(err);
      }
    });
  }

  deleteUser(userId: string) {
    const user = this.users.find(u => u.id === userId);
    if (user) {
      this.userToDelete = user;
      this.showDeleteModal = true;
    }
  }
  
  confirmDelete() {
    if (this.userToDelete) {
      this.adminService.deleteUser(this.userToDelete.id).subscribe({
        next: () => {
          this.loadUsers();
          this.closeDeleteModal();
        },
        error: (err) => {
          alert('Erreur lors de la suppression');
          console.error(err);
          this.closeDeleteModal();
        }
      });
    }
  }
  
  closeDeleteModal() {
    this.showDeleteModal = false;
    this.userToDelete = null;
  }

  editUser(user: UserManagement) {
    this.isEditMode = true;
    this.currentUser = { ...user };
    this.showModal = true;
  }
  
  openCreateModal() {
    this.isEditMode = false;
    this.currentUser = {
      fullName: '',
      email: '',
      roles: 'USER',
      status: 'ACTIVE',
      password: ''
    };
    this.showModal = true;
  }
  
  saveUser() {
    if (this.isEditMode && this.currentUser.id) {
      // Update existing user
      this.adminService.updateUser(this.currentUser.id, this.currentUser).subscribe({
        next: () => {
          this.loadUsers();
          this.closeModal();
        },
        error: (err) => {
          alert('Erreur lors de la mise à jour');
          console.error(err);
        }
      });
    } else {
      // Create new user
      this.adminService.createUser(this.currentUser).subscribe({
        next: () => {
          this.loadUsers();
          this.closeModal();
        },
        error: (err) => {
          const errorMessage = err.error?.data || err.error?.description || err.message;
          if (errorMessage.includes('Email already exists')) {
            alert('❌ Cet email existe déjà. Veuillez utiliser un autre email.');
          } else {
            alert('❌ Erreur lors de la création: ' + errorMessage);
          }
          console.error(err);
        }
      });
    }
  }
  
  closeModal() {
    this.showModal = false;
    this.isEditMode = false;
    this.currentUser = {
      fullName: '',
      email: '',
      roles: 'USER',
      status: 'ACTIVE',
      password: ''
    };
  }

  exportToExcel() {
    // Prepare data for export
    const exportData = this.filteredUsers.map(user => ({
      'ID': user.id,
      'Nom Complet': user.fullName,
      'Email': user.email,
      'Rôle': user.roles,
      'Plan': user.plan,
      'Statut': user.status,
      'Date d\'inscription': new Date(user.registrationDate).toLocaleDateString('fr-FR'),
      'Recherches utilisées': user.searchesUsed,
      'Quota de recherches': user.searchQuota,
      'Quota (%)': user.quotaPercentage.toFixed(2) + '%'
    }));

    // Convert to CSV (using semicolon for French Excel)
    const headers = Object.keys(exportData[0]);
    const csvContent = [
      headers.join(';'),
      ...exportData.map(row => 
        headers.map(header => {
          const value = row[header as keyof typeof row];
          // Escape values containing semicolons or quotes
          if (typeof value === 'string' && (value.includes(';') || value.includes('"') || value.includes('\n'))) {
            return `"${value.replace(/"/g, '""')}"`;
          }
          return value;
        }).join(';')
      )
    ].join('\n');

    // Create blob and download
    const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    link.setAttribute('href', url);
    link.setAttribute('download', `utilisateurs_${new Date().toISOString().split('T')[0]}.csv`);
    link.style.visibility = 'hidden';
    
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
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

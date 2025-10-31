import { Component, OnInit, OnDestroy } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

import { ScrapingService } from '../../../core/services/scraping.service';
import { NotificationService } from '../../../core/services/notification.service';

import { Profile } from '../../../core/models/profile.model';

@Component({
  selector: 'app-results',
  templateUrl: './results.component.html',
  styleUrls: ['./results.component.css']
})
export class ResultsComponent implements OnInit, OnDestroy {
  searchId: string | null = null;
  profiles: Profile[] = [];
  rawProfiles: any[] = [];
  isLoading = true;
  searchDetails: any = null;
  currentPage = 0;
  pageSize = 20;
  totalProfiles = 0;
  hasMoreProfiles = false;
  selectedProfile: any = null;

  private destroy$ = new Subject<void>();

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private scrapingService: ScrapingService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.route.queryParams
      .pipe(takeUntil(this.destroy$))
      .subscribe(params => {
        this.searchId = params['searchId'];
        if (this.searchId) {
          this.loadSearchResults();
        } else {
          this.notificationService.showError('No search ID provided');
          this.router.navigate(['/user/search']);
        }
      });
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  private loadSearchResults(): void {
    if (!this.searchId) return;

    this.isLoading = true;

    // Load search details first
    this.scrapingService.getScrapings()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (scrapings: any[]) => {
          const search = scrapings.find(s => s.id === this.searchId);
          if (search) {
            this.searchDetails = search;
            this.loadProfiles();
          } else {
            this.notificationService.showError('Search not found');
            this.router.navigate(['/user/search']);
          }
        },
        error: (error: any) => {
          console.error('Error loading search details:', error);
          this.notificationService.showError('Failed to load search details');
          this.isLoading = false;
        }
      });
  }

  private loadProfiles(): void {
    if (!this.searchId) return;

    this.scrapingService.getProfilesByScraping(this.searchId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (profiles: Profile[]) => {
          this.profiles = profiles;
          this.totalProfiles = profiles.length;
          this.hasMoreProfiles = profiles.length >= this.pageSize;
          this.isLoading = false;
          this.notificationService.showSuccess('Scraping terminé');
        },
        error: (error: any) => {
          console.error('Error loading profiles:', error);
          this.notificationService.showError('Failed to load profiles');
          this.isLoading = false;
        }
      });

    this.scrapingService.getRawProfilesByScraping(this.searchId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (items: any[]) => {
          this.rawProfiles = items;
          this.totalProfiles = items.length;
        },
        error: () => {}
      });
  }

  onLoadMore(): void {
    if (this.hasMoreProfiles) {
      this.currentPage++;
      // TODO: Implement pagination if backend supports it
      this.loadProfiles();
    }
  }

  onViewProfile(profile: Profile): void {
    // Navigate to profile detail view
    this.router.navigate(['/user/profile', profile.id]);
  }

  onExportResults(): void {
    const rows = this.rawProfiles.map(p => ({
      nom: p.nom || '',
      description: p.description || '',
      email: p.email || '',
      telephone: p.telephone || '',
      urlSource: p.urlSource || '',
      statut: p.statut || ''
    }));
    const header = ['nom','description','email','telephone','urlSource','statut'];
    const csv = [header.join(','), ...rows.map(r => header.map(h => `"${(r as any)[h].toString().replace(/"/g,'""')}"`).join(','))].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.href = url;
    link.download = 'profiles.csv';
    link.click();
    URL.revokeObjectURL(url);
    this.notificationService.showSuccess('Export généré');
  }

  onNewSearch(): void {
    this.router.navigate(['/user/search']);
  }

  onBookmarkProfile(profile: Profile): void {
    // TODO: Implement bookmark functionality
    this.notificationService.showSuccess('Profile bookmarked successfully');
  }

  onShareProfile(profile: Profile): void {
    // TODO: Implement share functionality
    const url = window.location.origin + '/user/profile/' + profile.id;
    navigator.clipboard.writeText(url).then(() => {
      this.notificationService.showSuccess('Profile link copied to clipboard');
    });
  }

  onDeleteProfile(p: any): void {
    if (!p?.publicId) return;
    this.scrapingService.deleteProfileByPublicId(p.publicId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.rawProfiles = this.rawProfiles.filter(x => `${x.publicId}` !== `${p.publicId}`);
          this.totalProfiles = this.rawProfiles.length;
          this.notificationService.showSuccess('Profil supprimé');
        },
        error: () => {
          this.notificationService.showError('Suppression échouée');
        }
      });
  }

  onDownloadProfile(p: any): void {
    const data = {
      nom: p?.nom || '',
      description: p?.description || '',
      email: p?.email || '',
      telephone: p?.telephone || '',
      urlSource: p?.urlSource || '',
      statut: p?.statut || ''
    } as any;
    const header = Object.keys(data);
    const csv = [header.join(','), header.map(h => `"${(data as any)[h].toString().replace(/"/g,'""')}"`).join(',')].join('\n');
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    const url = URL.createObjectURL(blob);
    link.href = url;
    link.download = `${(p?.nom || 'profil').toString().replace(/\s+/g,'_')}.csv`;
    link.click();
    URL.revokeObjectURL(url);
  }

  onBlockProfile(p: any): void {
    if (!p) return;
    p.statut = 'blocked';
    this.notificationService.showSuccess('Profil bloqué');
  }

  onDetailProfile(p: any): void {
    this.selectedProfile = p;
  }

  onCloseDetail(): void {
    this.selectedProfile = null;
  }

  getSearchCriteriaDisplay(): string {
    if (!this.searchDetails?.criteria) return '';

    const criteria = this.searchDetails.criteria;
    const parts = [];

    if (criteria.location) parts.push(criteria.location);
    if (criteria.keywords) parts.push(`"${criteria.keywords}"`);
    if (criteria.experience) parts.push(criteria.experience + ' level');
    if (criteria.company) parts.push(criteria.company);

    return parts.join(' • ');
  }

  getProfileMatchScore(profile: Profile): number {
    // TODO: Calculate match score based on search criteria
    return Math.floor(Math.random() * 40) + 60; // Placeholder
  }

  getProfileSkills(profile: Profile): string[] {
    // TODO: Extract skills from profile data
    return profile.skills || [];
  }

  getProfileExperience(profile: Profile): string {
    // TODO: Format experience data
    const list = (profile as any)?.experience as any[] || [];
    if (!Array.isArray(list) || list.length === 0) return 'Not specified';
    const latest = list[0] || {};
    const position = (latest as any).position || '';
    const company = (latest as any).company || '';
    return [position, company ? `@ ${company}` : ''].filter(Boolean).join(' ');
  }

  getProfileEducation(profile: Profile): string {
    // TODO: Format education data
    const list = (profile as any)?.education as any[] || [];
    if (!Array.isArray(list) || list.length === 0) return 'Not specified';
    const latest = list[0] || {};
    const degree = (latest as any).degree || '';
    const institution = (latest as any).institution || '';
    return [degree, institution ? `- ${institution}` : ''].filter(Boolean).join(' ');
  }

  formatDate(date: string | Date): string {
    return new Date(date).toLocaleDateString();
  }

  getStatusClass(status: string): string {
    switch (status?.toLowerCase()) {
      case 'completed':
        return 'status-completed';
      case 'processing':
        return 'status-processing';
      case 'failed':
        return 'status-failed';
      default:
        return '';
    }
  }

  get totalPages(): number {
    const total = this.rawProfiles.length;
    return Math.max(1, Math.ceil(total / this.pageSize));
  }

  get pagedRawProfiles(): any[] {
    const start = this.currentPage * this.pageSize;
    const end = start + this.pageSize;
    return this.rawProfiles.slice(start, end);
  }

  get pageStartIndex(): number {
    if (this.rawProfiles.length === 0) return 0;
    return this.currentPage * this.pageSize + 1;
  }

  get pageEndIndex(): number {
    return Math.min((this.currentPage + 1) * this.pageSize, this.rawProfiles.length);
  }

  get pages(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i);
  }

  firstPage(): void {
    if (this.currentPage !== 0) this.currentPage = 0;
  }

  prevPage(): void {
    if (this.currentPage > 0) this.currentPage--;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages - 1) this.currentPage++;
  }

  lastPage(): void {
    const last = this.totalPages - 1;
    if (this.currentPage !== last) this.currentPage = last;
  }

  goToPage(i: number): void {
    if (i < 0 || i > this.totalPages - 1) return;
    this.currentPage = i;
  }

  trackByProfileId(index: number, profile: Profile): any {
    return profile.id;
  }
}

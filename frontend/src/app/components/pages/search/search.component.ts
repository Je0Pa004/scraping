import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ScrapingService } from '../../../core/services/scraping.service';

@Component({
  selector: 'app-search',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {
  form: FormGroup;
  isSubmitting = false;
  history: any[] = [];
  confirmDeleteOpen = false;
  deleteTarget: any = null;
  deleting = false;

  constructor(private fb: FormBuilder, private scrapingService: ScrapingService, private router: Router) {
    this.form = this.fb.group({
      title: [''],
      skills: [''],
      location: [''],
      industry: [''],
      company: [''],
      companySize: [''],
      source: ['GOOGLE_MAP', [Validators.required]],
      maxResults: [50, [Validators.min(1), Validators.max(500)]]
    });
  }

  ngOnInit(): void {
    this.loadHistory();
  }

  private loadHistory() {
    this.scrapingService.getScrapings().subscribe(items => {
      this.history = items || [];
    });
  }

  openDelete(s: any) {
    this.deleteTarget = s;
    this.confirmDeleteOpen = true;
    try { console.log('Open delete for', s); } catch {}
  }

  closeDelete() {
    this.confirmDeleteOpen = false;
    this.deleteTarget = null;
  }

  confirmDelete() {
    const publicId = this.deleteTarget?.id;
    const numericId = this.deleteTarget?.numericId;
    if (!publicId && !numericId) {
      this.closeDelete();
      return;
    }
    if (this.deleting) return;
    this.deleting = true;
    const req$ = numericId ? this.scrapingService.deleteScrapingById(numericId) : this.scrapingService.deleteScrapingByPublicId(publicId);
    req$.subscribe({
      next: () => {
        this.deleting = false;
        this.closeDelete();
        this.loadHistory();
        try { console.log('Delete scraping success'); } catch {}
      },
      error: (err) => {
        this.deleting = false;
        try { console.error('Delete scraping error', err); } catch {}
        alert('Erreur lors de la suppression');
        this.closeDelete();
      }
    });
  }

  onSubmit() {
    if (this.form.invalid || this.isSubmitting) return;
    this.isSubmitting = true;
    const payload = {
      title: this.form.value.title,
      keywords: this.form.value.title,
      location: this.form.value.location,
      industry: this.form.value.industry,
      company: this.form.value.company,
      companySize: this.form.value.companySize,
      source: this.form.value.source,
      maxResults: this.form.value.maxResults
    };
    this.scrapingService.performScraping(payload).subscribe({
      next: (res: any) => {
        this.isSubmitting = false;
        if (res?.id) {
          // refresh history
          this.loadHistory();
          this.router.navigate(['/app/results'], { queryParams: { searchId: res.id } });
        }
      },
      error: () => {
        this.isSubmitting = false;
      }
    });
  }
}

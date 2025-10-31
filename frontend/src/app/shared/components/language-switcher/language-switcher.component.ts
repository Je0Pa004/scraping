import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LanguageService, Language } from '../services/language.service';
import { Subscription } from 'rxjs';

@Component({
  selector: 'app-language-switcher',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="language-switcher">
      <div class="dropdown">
        <button class="dropdown-toggle" (click)="toggleDropdown()">
          <span class="flag">{{ currentLanguage?.flag }}</span>
          <span class="language-name">{{ currentLanguage?.name }}</span>
          <span class="dropdown-arrow">▼</span>
        </button>

        <div class="dropdown-menu" [class.show]="isDropdownOpen">
          <div
            class="dropdown-item"
            *ngFor="let language of languages"
            (click)="selectLanguage(language)"
            [class.active]="language.code === currentLanguage?.code"
          >
            <span class="flag">{{ language.flag }}</span>
            <span class="language-name">{{ language.name }}</span>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .language-switcher {
      position: relative;
    }

    .dropdown {
      position: relative;
    }

    .dropdown-toggle {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.5rem 1rem;
      background: white;
      border: 1px solid #ddd;
      border-radius: 4px;
      cursor: pointer;
      font-size: 0.9rem;
      color: #333;
      transition: all 0.2s ease;
    }

    .dropdown-toggle:hover {
      background: #f8f9fa;
      border-color: #007bff;
    }

    .flag {
      font-size: 1.2rem;
    }

    .language-name {
      font-weight: 500;
    }

    .dropdown-arrow {
      font-size: 0.7rem;
      color: #666;
    }

    .dropdown-menu {
      position: absolute;
      top: 100%;
      right: 0;
      background: white;
      border: 1px solid #ddd;
      border-radius: 4px;
      box-shadow: 0 2px 10px rgba(0,0,0,0.1);
      min-width: 150px;
      z-index: 1000;
      opacity: 0;
      visibility: hidden;
      transform: translateY(-10px);
      transition: all 0.2s ease;
    }

    .dropdown-menu.show {
      opacity: 1;
      visibility: visible;
      transform: translateY(0);
    }

    .dropdown-item {
      display: flex;
      align-items: center;
      gap: 0.5rem;
      padding: 0.75rem 1rem;
      cursor: pointer;
      transition: background-color 0.2s ease;
    }

    .dropdown-item:hover {
      background: #f8f9fa;
    }

    .dropdown-item.active {
      background: #e3f2fd;
      color: #007bff;
    }

    .dropdown-item:first-child {
      border-radius: 4px 4px 0 0;
    }

    .dropdown-item:last-child {
      border-radius: 0 0 4px 4px;
    }
  `]
})
export class LanguageSwitcherComponent implements OnInit, OnDestroy {
  languages: Language[] = [];
  currentLanguage: Language | null = null;
  isDropdownOpen = false;
  private subscription?: Subscription;

  constructor(private languageService: LanguageService) {}

  ngOnInit() {
    this.languages = this.languageService.getLanguages();

    this.subscription = this.languageService.currentLanguage$.subscribe(languageCode => {
      this.currentLanguage = this.languages.find(lang => lang.code === languageCode) || this.languages[0];
    });
  }

  ngOnDestroy() {
    this.subscription?.unsubscribe();
  }

  toggleDropdown() {
    this.isDropdownOpen = !this.isDropdownOpen;
  }

  selectLanguage(language: Language) {
    this.languageService.switchLanguage(language.code);
    this.isDropdownOpen = false;
  }

  // Close dropdown when clicking outside
  onDocumentClick(event: Event) {
    if (!this.isDropdownOpen) return;

    const target = event.target as HTMLElement;
    const dropdown = target.closest('.dropdown');
    if (!dropdown) {
      this.isDropdownOpen = false;
    }
  }
}

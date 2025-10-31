export interface SearchCriteria {
  keywords: string[];
  location: string;
  experienceLevel: 'ENTRY' | 'MID' | 'SENIOR' | 'EXECUTIVE';
  skills: string[];
  company: string;
  industry: string;
  salaryMin?: number;
  salaryMax?: number;
  remoteWork: boolean;
  jobType: 'FULL_TIME' | 'PART_TIME' | 'CONTRACT' | 'FREELANCE';
}

export interface SearchResult {
  id: string;
  title: string;
  company: string;
  location: string;
  salary?: string;
  description: string;
  requirements: string[];
  postedDate: Date;
  applicationUrl: string;
  source: string;
  matchScore: number;
}

export interface ScrapingJob {
  id: number;
  userId: number;
  criteria: SearchCriteria;
  status: 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED';
  resultsCount: number;
  createdAt: Date;
  updatedAt: Date;
  completedAt?: Date;
  results: SearchResult[];
}

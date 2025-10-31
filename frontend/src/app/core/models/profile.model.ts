export interface Profile {
  id: number;
  userId: number;
  firstName: string;
  lastName: string;
  phone: string;
  address: string;
  city: string;
  country: string;
  linkedinUrl?: string;
  githubUrl?: string;
  portfolioUrl?: string;
  bio: string;
  skills: string[];
  experience: Experience[];
  education: Education[];
  createdAt: Date;
  updatedAt: Date;
}

export interface Experience {
  id: number;
  company: string;
  position: string;
  startDate: Date;
  endDate?: Date;
  description: string;
  isCurrent: boolean;
}

export interface Education {
  id: number;
  institution: string;
  degree: string;
  field: string;
  startDate: Date;
  endDate?: Date;
  grade?: string;
}

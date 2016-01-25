\NeedsTeXFormat{LaTeX2e}
\ProvidesClass{cseminar}[2015/09/01 v1.0 Department of Computer Science Seminar Template]

% Written by Pasi Eronen (pe at iki dot fi) for T-110.551 spring 2004
% Changes for Spring 2005 made by Nadja Kasinskaja, 2005-01-12
% Changes for Spring 2006 made by Sanna Liimatainen, 2006-01-12
% Changes for Autumn 2007 made by Laura Takkinen, 2007-09-17
% Changes for Autumn 2008 made by Jani Heikkinen, 2008-08-22
% Changes for Spring 2010 made by Sachin Gaur, 2009-12-14
% Updates for Autumn 2012 made by Mario Di Francesco
% Changes for Autumn 2010 - Spring 2014 made by Sandeep Tamrakar
% Updates for Spring 2014 - Fall 2015 made by Mario Di Francesco

\newif\if@cameraready
\@camerareadyfalse
\DeclareOption{cameraready}{\@camerareadytrue}
\ProcessOptions

% User defined
\def\@coursename{}
\def\@courseyear{\the\year}
\def\@coursesemester{}

\newcommand{\coursename}[1]{\def\@coursename{#1}}
\newcommand{\courseyear}[2][]{\def\@coursesemester{#1}\def\@courseyear{#2}}

\coursename{Design Document: Java API for Trusted Applications}
\courseyear[Spring]{2016}

% Basic stuff
\if@cameraready
  \LoadClass[10pt,twocolumn,a4paper]{article}
\else
  \LoadClass[10pt, a4paper]{article}
\fi
\RequirePackage{times}
\RequirePackage[T1]{fontenc}
\RequirePackage{graphicx}
\RequirePackage{fancyhdr}
\RequirePackage{url}
\RequirePackage{setspace}

% Adjust margins
\addtolength{\textwidth}{14mm}
\addtolength{\hoffset}{-7mm}
\addtolength{\textheight}{45mm}
\addtolength{\voffset}{-18mm}

% Abbreviated bibliography style
\bibliographystyle{abbrv}

% Line spacing for draft and final papers
\if@cameraready
  \singlespacing
\else
  \doublespacing
\fi
 
%isodate from manuals of University of Oulu, time removed
%http://www.oulu.fi/atkk/tkpalv/unix/isodate.html
%referred 2004-02-18
%added and dates in the document changed by Nadja Kasinskaja

\newcommand{\twodigits}[1]{\ifnum #1<10 0\fi \number#1}

\newcommand{\isodate}{%
  \the\year-\twodigits{\month}-\twodigits{\day}}

%endof isodate

\pagestyle{fancyplain}
\addtolength{\headsep}{-10pt}
\fancyhead{}
\fancyfoot{}
\fancyhead[LO]{\fancyplain{}{\@coursename}}
\if@cameraready
  \fancyhead[RO]{\fancyplain{}{\@coursesemester~\@courseyear}}
\else
  \fancyhead[RO]{\fancyplain{}
                 %%{Draft {\number\day}.{\number\month}.{\number\year}}}
                 {Draft \isodate}}    
  \fancyfoot[CO]{\thepage}
\fi
\date{}

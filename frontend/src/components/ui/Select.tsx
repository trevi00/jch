import React from 'react';

interface SelectOption {
  value: string;
  label: string;
}

interface SelectProps {
  value: string;
  onValueChange: (value: string) => void;
  children: React.ReactNode;
}

interface SelectContentProps {
  children: React.ReactNode;
}

interface SelectItemProps {
  value: string;
  children: React.ReactNode;
}

interface SelectTriggerProps {
  children: React.ReactNode;
}

interface SelectValueProps {
  placeholder?: string;
}

export const Select: React.FC<SelectProps> = ({ value, onValueChange, children }) => {
  return (
    <div className="relative">
      {React.Children.map(children, (child) => {
        if (React.isValidElement(child)) {
          return React.cloneElement(child, { value, onValueChange } as any);
        }
        return child;
      })}
    </div>
  );
};

export const SelectTrigger: React.FC<SelectTriggerProps & { value?: string; onValueChange?: (value: string) => void }> = ({ 
  children, 
  value, 
  onValueChange 
}) => {
  const [isOpen, setIsOpen] = React.useState(false);
  
  return (
    <div className="relative">
      <button
        type="button"
        onClick={() => setIsOpen(!isOpen)}
        className="flex h-10 w-full items-center justify-between rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring focus:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50"
      >
        {children}
        <svg
          className="h-4 w-4 opacity-50"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          strokeWidth="2"
          strokeLinecap="round"
          strokeLinejoin="round"
        >
          <path d="m6 9 6 6 6-6"/>
        </svg>
      </button>
      {isOpen && (
        <div className="absolute top-full left-0 right-0 z-50 mt-1 bg-white border border-gray-200 rounded-md shadow-lg">
          {React.Children.map(children, (child) => {
            if (React.isValidElement(child) && child.type === SelectContent) {
              return React.cloneElement(child, { 
                value, 
                onValueChange: (newValue: string) => {
                  onValueChange?.(newValue);
                  setIsOpen(false);
                }
              } as any);
            }
            return null;
          })}
        </div>
      )}
    </div>
  );
};

export const SelectContent: React.FC<SelectContentProps & { value?: string; onValueChange?: (value: string) => void }> = ({ 
  children, 
  value, 
  onValueChange 
}) => {
  return (
    <div className="p-1">
      {React.Children.map(children, (child) => {
        if (React.isValidElement(child)) {
          return React.cloneElement(child, { value, onValueChange } as any);
        }
        return child;
      })}
    </div>
  );
};

export const SelectItem: React.FC<SelectItemProps & { value?: string; onValueChange?: (value: string) => void }> = ({ 
  value: itemValue, 
  children, 
  value: selectedValue, 
  onValueChange 
}) => {
  const isSelected = selectedValue === itemValue;
  
  return (
    <button
      type="button"
      onClick={() => onValueChange?.(itemValue)}
      className={`w-full text-left px-2 py-1.5 text-sm rounded hover:bg-gray-100 ${
        isSelected ? 'bg-gray-100 font-medium' : ''
      }`}
    >
      {children}
    </button>
  );
};

export const SelectValue: React.FC<SelectValueProps & { value?: string }> = ({ placeholder, value }) => {
  if (value) {
    // Find the display text for the selected value
    return <span>{value}</span>;
  }
  
  return <span className="text-gray-500">{placeholder || 'Select an option...'}</span>;
};
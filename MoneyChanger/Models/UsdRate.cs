using System;
using System.Collections.Generic;

namespace MoneyChanger.Models;

public partial class UsdRate
{
    public int Id { get; set; }

    public int CurrencyId { get; set; }

    public decimal Rate { get; set; }

    public virtual Currency Currency { get; set; } = null!;
}

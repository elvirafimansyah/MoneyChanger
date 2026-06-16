using System;
using System.Collections.Generic;

namespace MoneyChanger.Models;

public partial class Currency
{
    public int Id { get; set; }

    public string Country { get; set; } = null!;

    public string Name { get; set; } = null!;

    public string Abbreviation { get; set; } = null!;

    public virtual ICollection<Order> OrderOriginCurrencies { get; set; } = new List<Order>();

    public virtual ICollection<Order> OrderTargetCurrencies { get; set; } = new List<Order>();

    public virtual ICollection<UsdRate> UsdRates { get; set; } = new List<UsdRate>();
}
